package com.petpet.c3po.adaptor.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;

import com.petpet.c3po.adaptor.rules.drools.ConflictCollector;
import com.petpet.c3po.adaptor.rules.drools.ElementModificationListener;
import com.petpet.c3po.adaptor.rules.drools.LogCollector;
import com.petpet.c3po.adaptor.rules.drools.RuleActivationListener;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.dao.MetadataUtil;
import com.petpet.c3po.datamodel.Element;

public class DroolsConflictResolutionProcessingRule implements
    PostProcessingRule {

  public static final int PRIORITY = 500;

  private static final String G_LOGOUPUTCOLLECTOR = "logger";
  private static final String G_METADATAUTIL = "util";
  private static final String G_CONFLICTCOLLECTOR = "conflicts";
  private static final String G_BASICRULESLOGLEVEL = "loglevel";

  private static final int MIN_LOGLEVEL = LogCollector.INFO;
  private static final int RULESLOGLEVEL = LogCollector.DEBUG;

  private final Cache cache;
  private Map<Thread, StatelessKnowledgeSession> sessions;
  private RuleActivationListener ruleActivationListener;

  private KnowledgeBase kbase;

  private ConflictCollector conflictCollector;

  private MetadataUtil metadataUtil;

  public DroolsConflictResolutionProcessingRule(Cache cache) {
    this.cache = cache;

    // read in the source
    List<String> filenames = new ArrayList<String>();
    filenames.add("/rules/conflictResolutionBasicRules.drl");
    filenames.add("/rules/conflictResolutionFormatMime.drl");
    filenames.add("/rules/conflictResolution.drl");

    this.initKnowledgeBase(filenames);

    this.ruleActivationListener = new RuleActivationListener(
        this.kbase.getKnowledgePackages());
    this.metadataUtil = new MetadataUtil(this.cache);

    this.conflictCollector = new ConflictCollector(this.metadataUtil);
    this.sessions = new ConcurrentHashMap<Thread, StatelessKnowledgeSession>();
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public void onCommandFinished() {
    // TODO: make the execution of these 2 methods configurable
    this.ruleActivationListener.printStatistics(System.out);
    this.conflictCollector.printStatistics(System.out);
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
        G_LOGOUPUTCOLLECTOR);

    // we set the output of the modification listener to trace
    session.addEventListener(new ElementModificationListener(outputCollector,
        LogCollector.TRACE));

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
    session.setGlobal(G_METADATAUTIL, this.metadataUtil);
    session.setGlobal(G_CONFLICTCOLLECTOR, this.conflictCollector);

    // TODO: make MIN_LOGLEVEL configurable (verbosity level)
    session.setGlobal(G_LOGOUPUTCOLLECTOR, new LogCollector(this.cache,
        MIN_LOGLEVEL));
    // This is the default rule log level (in DRL files: globals loglevel
    session.setGlobal(G_BASICRULESLOGLEVEL, RULESLOGLEVEL);

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

}
