package com.petpet.c3po.adaptor.rules.drools;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatelessKnowledgeSession;

import com.petpet.c3po.api.model.Element;

public class DroolsResolutionWorker {

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

  private StatelessKnowledgeSession session;

  protected DroolsResolutionWorker(KnowledgeBase knowledgeBase) {

    StatelessKnowledgeSession session = knowledgeBase
        .newStatelessKnowledgeSession();
    session.setGlobal(G_CONFLICTCOLLECTOR, ConflictCollector.getInstance());

    // TODO: make MIN_LOGLEVEL configurable (verbosity level)
    session.setGlobal(G_LOGOUPUTCOLLECTOR, new LogCollector(MIN_LOGLEVEL + 1));
    // This is the default rule log level (in DRL files: globals loglevel)
    session.setGlobal(G_BASICRULESLOGLEVEL, RULESLOGLEVEL);

    this.session = session;
  }

  public void process(Element e) {

    LogCollector outputCollector = (LogCollector) this.session.getGlobals()
        .get(G_LOGOUPUTCOLLECTOR);

    // listeners need to be re-added before every execution
    this.session.addEventListener(new ElementModificationListener(
        outputCollector, LogCollector.TRACE));
    this.session.addEventListener(RuleActivationListener.getInstance());

    this.session.execute(e);

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

  }

}