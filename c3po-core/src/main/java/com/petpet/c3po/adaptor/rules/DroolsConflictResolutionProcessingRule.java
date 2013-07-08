package com.petpet.c3po.adaptor.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.petpet.c3po.api.adaptor.PostProcessingRule;

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
import com.petpet.c3po.api.model.Element;

public class DroolsConflictResolutionProcessingRule implements
    PostProcessingRule {

  public static final int PRIORITY = 500;

  /**
   * The name of the global variable containing the {@link LogCollector}.
   */
  private static final String G_LOGOUPUTCOLLECTOR = "logger";

  /**
   * The name of the global variable containing the {@link ConflictCollector}.
   */
  private static final String G_CONFLICTCOLLECTOR = "conflictcollector";

  /**
   * The name of the global variable containing the log level.
   * 
   * @see LogCollector#log(int, String)
   */
  private static final String G_BASICRULESLOGLEVEL = "loglevel";

  /**
   * TODO: find a better logging mechanism that fits to the rest of C3PO
   */
  // private static final int MIN_LOGLEVEL = LogCollector.INFO + 1;
  private static final int MIN_LOGLEVEL = LogCollector.DEBUG;
  private static final int RULESLOGLEVEL = LogCollector.DEBUG;

  /**
   * Hold a stateless session for each thread to allow multithreading without
   * side-effects between threads.
   */
  private Map<Thread, StatelessKnowledgeSession> sessions;

  /**
   * The {@link KnowledgeBase} holding all compiled rules.
   */
  private KnowledgeBase kbase;

  public DroolsConflictResolutionProcessingRule() {

    // read in the source
    // TODO: make this configurable/extendable by the user via commandline parameters
    List<String> filenames = new ArrayList<String>();
    filenames.add("/rules/conflictResolutionBasicRules.drl");
    filenames.add("/rules/conflictResolutionFormatMime.drl");
    filenames.add("/rules/conflictResolution.drl");

    this.initKnowledgeBase(filenames);

    RuleActivationListener.getInstance().initialize(
        this.kbase.getKnowledgePackages());

    this.sessions = new ConcurrentHashMap<Thread, StatelessKnowledgeSession>();
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public void onCommandFinished() {
    // TODO: make the execution of these methods configurable
    RuleActivationListener.getInstance().printStatistics(System.out, false);
    ConflictCollector.getInstance().printAccumulatedStatistics(System.out, false);
    ConflictCollector.getInstance().printStatistics(System.out, false);
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

    // listeners need to be re-added before every execution
    session.addEventListener(new ElementModificationListener(outputCollector,
        LogCollector.TRACE));
    session.addEventListener(RuleActivationListener.getInstance());
    
    session.execute(e);

    String logOutput = outputCollector.reset();

    /** 
     * TODO: use proper logging, not stdout
     **/
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
    session.setGlobal(G_CONFLICTCOLLECTOR, ConflictCollector.getInstance());

    // TODO: make MIN_LOGLEVEL configurable (verbosity level)
    session.setGlobal(G_LOGOUPUTCOLLECTOR, new LogCollector(MIN_LOGLEVEL));
    // This is the default rule log level (in DRL files: globals loglevel)
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
      /*TODO: proper handling/logging of errors! */
      System.err.println(kbuilder.getErrors().toString());
    }

    this.kbase = KnowledgeBaseFactory.newKnowledgeBase();
    this.kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

  }

}
