package com.petpet.c3po.adaptor.rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.datamodel.Element;

public class DroolsConflictResolutionProcessingRule implements
    PostProcessingRule {

  public static final int PRIORITY = 500;

  private static final String CACHE = "cache";

  private final Cache cache;
  private StatelessKnowledgeSession session;
  private RuleActivationListener ruleActivationListener;

  private ElementModificationListener elementModificationListener;

  public DroolsConflictResolutionProcessingRule(Cache cache) {
    this.cache = cache;

    // read in the source
    String filename = "/rules/conflictResolution.drl";
    List<String> filenames = Arrays.asList(filename);

    this.initSession(filenames);

  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public void onCommandFinished() {
    this.ruleActivationListener.printStatistics();
  }

  @Override
  public Element process(Element e) {

    // TODO: this is only to debug the output - remove it when done!
    synchronized (System.out) {
      // TODO check this for unsynchronized behaviour!
      this.session.addEventListener(this.ruleActivationListener);
      this.session.addEventListener(this.elementModificationListener);
      this.session.execute(e);
    }

    return e;
  }

  private void initSession(List<String> filenames) {
    KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

    for (String filename : filenames) {
      kbuilder.add(
          ResourceFactory.newClassPathResource(filename, this.getClass()),
          ResourceType.DRL);
    }

    if (kbuilder.hasErrors()) {
      System.err.println(kbuilder.getErrors().toString());
    }

    KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
    kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

    this.session = kbase.newStatelessKnowledgeSession();

    this.ruleActivationListener = this.new RuleActivationListener(
        kbase.getKnowledgePackages());
    this.elementModificationListener = this.new ElementModificationListener();

    this.session.setGlobal(CACHE, this.cache);
  }

  public class ElementModificationListener implements
      WorkingMemoryEventListener {

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

    private void startTrackingElementChanges(Element element) {
      BasicDBObject document = element.getDocument();
      BasicDBObject metadata = (BasicDBObject) document.get("metadata");
    }

    private void stopTrackingElement(Element removedElement) {

    }

    private void trackElementUpdate(Element modifiedElement, Rule rule) {
      BasicDBObject document = modifiedElement.getDocument();
      BasicDBObject metadataObject = (BasicDBObject) document.get("metadata");
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

      System.err.println("after activation: " + firedRule);

      Integer counter = this.activations.get(firedRule);
      counter++;
      this.activations.put(firedRule, counter);

    }

    public void printStatistics() {
      System.out.println("======================================");
      System.out.println("Drools Conflict Resolution Statistics:");

      for (KnowledgePackage rulesPackage : this.packages) {
        System.out.println("--------------------------------------");
        System.out.println("Package: '" + rulesPackage.getName() + "'");
        for (Rule rule : rulesPackage.getRules()) {
          rule = this.unwrapRule(rule);
          System.out.println("* '" + rule.getName() + "'");
          System.out.println("  #:" + this.activations.get(rule));
        }
      }

      System.out.println("======================================");
    }

    public Rule unwrapRule(Rule rule) {
      if (rule instanceof RuleImpl) {
        rule = ((RuleImpl) rule).getRule();
      }
      return rule;
    }

  }

}
