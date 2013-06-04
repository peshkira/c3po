package com.petpet.c3po.adaptor.rules;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.definitions.rule.impl.RuleImpl;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.event.rule.ObjectInsertedEvent;
import org.drools.event.rule.ObjectRetractedEvent;
import org.drools.event.rule.ObjectUpdatedEvent;
import org.drools.event.rule.WorkingMemoryEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;

import com.mongodb.BasicDBObject;
import com.petpet.c3po.adaptor.rules.drools.LogCollector;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.dao.MetadataUtil;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.LogEntry.ChangeType;

public class DroolsConflictResolutionProcessingRule implements
    PostProcessingRule {

  public static final int PRIORITY = 500;

  private static final String LOGOUPUTCOLLECTOR = "log";

  private static final String METADATAUTIL = "util";

  private final Cache cache;
  private Map<Thread, StatelessKnowledgeSession> sessions;
  private RuleActivationListener ruleActivationListener;

  private KnowledgeBase kbase;

  public DroolsConflictResolutionProcessingRule(Cache cache) {
    this.cache = cache;

    // read in the source
    List<String> filenames = new ArrayList<String>();
    filenames.add("/rules/conflictResolution.drl");
    filenames.add("/rules/conflictResolutionBasicRules.drl");

    this.initKnowledgeBase(filenames);

    this.ruleActivationListener = this.new RuleActivationListener(
        this.kbase.getKnowledgePackages());

    this.sessions = new ConcurrentHashMap<Thread, StatelessKnowledgeSession>();
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public void onCommandFinished() {
    this.ruleActivationListener.printStatistics(System.out);
  }

  @Override
  public Element process(Element e) {

    StatelessKnowledgeSession session = this.sessions.get(Thread
        .currentThread());
    if (session == null) {
      session = this.createSession();
      this.sessions.put(Thread.currentThread(), session);
    }

    LogCollector outputCollector = (LogCollector) session.getGlobals().get(
        LOGOUPUTCOLLECTOR);
    session.addEventListener(new ElementModificationListener(outputCollector));
    session.addEventListener(this.ruleActivationListener);
    session.execute(e);

    String logOutput = outputCollector.reset();

    if (!logOutput.trim().isEmpty()) {
      synchronized (System.out) {
        System.out.println("======================================");
        System.out.println("Drools log of " + e.getUid());
        System.out.println();
        System.out.println(logOutput);
        System.out.println("======================================");
      }
    }
    return e;
  }

  private StatelessKnowledgeSession createSession() {
    StatelessKnowledgeSession session = this.kbase
        .newStatelessKnowledgeSession();
    session.setGlobal(METADATAUTIL, new MetadataUtil(this.cache));
    session.setGlobal(LOGOUPUTCOLLECTOR, new LogCollector(this.cache));

    return session;
  }

  private void initKnowledgeBase(List<String> filenames) {
    KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

    for (String filename : filenames) {
      kbuilder.add(
          ResourceFactory.newClassPathResource(filename, this.getClass()),
          ResourceType.DRL);
    }

    if (kbuilder.hasErrors()) {
      System.err.println(kbuilder.getErrors().toString());
    }

    this.kbase = KnowledgeBaseFactory.newKnowledgeBase();
    this.kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

  }

  public class ElementModificationListener implements
      WorkingMemoryEventListener {

    private Map<Element, BasicDBObject> memory = new ConcurrentHashMap<Element, BasicDBObject>();
    private LogCollector logCollector;

    protected ElementModificationListener(LogCollector logCollector) {
      super();
      this.logCollector = logCollector;
    }

    @Override
    public void objectInserted(ObjectInsertedEvent event) {

      Object insertedObject = event.getObject();
      if (insertedObject instanceof Element) {
        Element insertedElement = (Element) insertedObject;

        // Element was inserted, which has initial metadata
        this.startTrackingElementChanges(insertedElement);
      }
    }

    @Override
    public void objectRetracted(ObjectRetractedEvent event) {
      Object removedObject = event.getOldObject();
      if (removedObject instanceof Element) {
        Element removedElement = (Element) removedObject;

        // Element was removed, so we dont need to track metadata changes
        // anymore.
        this.stopTrackingElement(removedElement);
      }
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
      Rule rule = event.getPropagationContext().getRule();

      Object modifiedObject = event.getObject();
      if (modifiedObject instanceof Element) {
        Element modifiedElement = (Element) modifiedObject;

        // rule modified Element, which has (new/updated) metadata
        this.trackElementUpdate(modifiedElement, rule);
      }
    }

    private void startTrackingElementChanges(Element insertedElement) {
      BasicDBObject document = insertedElement.getDocument();
      BasicDBObject metadata = (BasicDBObject) document.get("metadata");

      this.memory.put(insertedElement, metadata);
    }

    private void stopTrackingElement(Element removedElement) {
      this.memory.remove(removedElement);
    }

    private void trackElementUpdate(Element modifiedElement, Rule rule) {
      BasicDBObject document = modifiedElement.getDocument();
      BasicDBObject newMetadata = (BasicDBObject) document.get("metadata");
      BasicDBObject oldMetadata = this.memory.get(modifiedElement);

      for (Entry<String, Object> oldMetadataEntry : oldMetadata.entrySet()) {
        String propertyId = oldMetadataEntry.getKey();
        BasicDBObject propertyData = (BasicDBObject) oldMetadataEntry
            .getValue();

        BasicDBObject newPropertyData = (BasicDBObject) newMetadata
            .remove(propertyId);
        if (newPropertyData == null) {
          // data is removed
          this.logCollector.debug("|Removed Info: " + propertyId + " - "
              + propertyData);
          modifiedElement.addLog(propertyId, propertyData.toString(),
              ChangeType.IGNORED, rule.getName());
        } else if (!propertyData.equals(newPropertyData)) {
          // data is changed
          this.logCollector.debug("|changed Info: " + propertyId);
          this.logCollector.debug("|   old value: " + propertyData);
          this.logCollector.debug("|   new value: " + newPropertyData);

          modifiedElement.addLog(propertyId, propertyData.toString(),
              ChangeType.UPDATED, rule.getName());

        } else {
          // data is unchanged
        }
      }

      for (Entry<String, Object> newMetadataEntry : newMetadata.entrySet()) {
        // property was added
        String propertyId = newMetadataEntry.getKey();
        Object propertyData = newMetadataEntry.getValue();

        this.logCollector.debug("|Added Info: " + propertyId + " - "
            + propertyData);

        modifiedElement
            .addLog(propertyId, "", ChangeType.ADDED, rule.getName());
      }

      // update memory - recreate new Document
      document = modifiedElement.getDocument();
      newMetadata = (BasicDBObject) document.get("metadata");
      this.memory.put(modifiedElement, newMetadata);

    }

  }

  public class RuleActivationListener extends DefaultAgendaEventListener {

    private Map<Rule, Integer> activations;
    private Collection<KnowledgePackage> packages;

    public RuleActivationListener(Collection<KnowledgePackage> knowledgePackages) {
      this.packages = knowledgePackages;
      this.activations = new HashMap<Rule, Integer>();

      for (KnowledgePackage knowledgePackage : knowledgePackages) {
        for (Rule rule : knowledgePackage.getRules()) {
          // get the REAL rule object from the wrapper
          rule = this.unwrapRule(rule);

          this.activations.put(rule, 0);
        }
      }
    }

    @Override
    public synchronized void afterActivationFired(
        AfterActivationFiredEvent event) {
      Rule firedRule = event.getActivation().getRule();

      Integer counter = this.activations.get(firedRule);
      counter++;
      this.activations.put(firedRule, counter);

    }

    public void printStatistics(PrintStream output) {
      output.println("======================================");
      output.println("Drools Conflict Resolution Statistics:");

      for (KnowledgePackage rulesPackage : this.packages) {
        output.println("--------------------------------------");
        output.println("Package: '" + rulesPackage.getName() + "'");
        for (Rule rule : rulesPackage.getRules()) {
          rule = this.unwrapRule(rule);
          output.println("* '" + rule.getName() + "'");
          output.println("  #:" + this.activations.get(rule));
        }
      }

      output.println("======================================");
    }

    public Rule unwrapRule(Rule rule) {
      if (rule instanceof RuleImpl) {
        rule = ((RuleImpl) rule).getRule();
      }
      return rule;
    }

  }

}
