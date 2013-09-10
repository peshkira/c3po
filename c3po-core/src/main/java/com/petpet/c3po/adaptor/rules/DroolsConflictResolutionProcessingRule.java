package com.petpet.c3po.adaptor.rules;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.petpet.c3po.adaptor.rules.drools.ConflictCollector;
import com.petpet.c3po.adaptor.rules.drools.DroolsResolutionWorker;
import com.petpet.c3po.adaptor.rules.drools.DroolsResolutionWorkerFactory;
import com.petpet.c3po.adaptor.rules.drools.RuleActivationListener;
import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;

public class DroolsConflictResolutionProcessingRule implements
    PostProcessingRule {

  public static final int PRIORITY = 500;

  /**
   * Hold a stateless session for each thread to allow multithreading without
   * side-effects between threads.
   */
  private Map<Thread, DroolsResolutionWorker> workers;

  private DroolsResolutionWorkerFactory factory;

  public DroolsConflictResolutionProcessingRule() {

    this.factory = new DroolsResolutionWorkerFactory();
    this.workers = new ConcurrentHashMap<Thread, DroolsResolutionWorker>();

    // read in the source

    try {

      // TODO: make this configurable/extendable by the user via commandline
      // parameters
      URL url = this.getClass().getResource("/rules/additionals");

      this.factory.setSource(new File(url.toURI()));

    } catch (URISyntaxException e) {
      throw new IllegalStateException(
          "Unable to access directory in classpath!", e);
    }
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public void onCommandFinished() {
    // TODO: make the execution of these methods configurable
    RuleActivationListener.getInstance().printStatistics(System.out, false);
    ConflictCollector.getInstance().printAccumulatedStatistics(System.out,
        false);
    ConflictCollector.getInstance().printStatistics(System.out, false);
  }

  @Override
  public Element process(Element e) {

    DroolsResolutionWorker worker = this.workers.get(Thread.currentThread());
    if (worker == null) {
      worker = this.factory.createWorker();
      this.workers.put(Thread.currentThread(), worker);
    }

    worker.process(e);

    return e;
  }
}
