package com.petpet.c3po.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.adaptor.fits.FITSAdaptor;
import com.petpet.c3po.adaptor.rules.EmptyValueProcessingRule;
import com.petpet.c3po.adaptor.rules.FormatVersionResolutionRule;
import com.petpet.c3po.adaptor.rules.HtmlInfoProcessingRule;
import com.petpet.c3po.adaptor.rules.ProcessingRule;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.ActionLog;
import com.petpet.c3po.datamodel.DigitalObjectStream;
import com.petpet.c3po.gatherer.FileSystemGatherer;
import com.petpet.c3po.utils.ActionLogHelper;

//TODO generalize the gatherer with the interface.
public class Controller {

  private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
  private PersistenceLayer persistence;
  private ExecutorService pool;
  private FileSystemGatherer gatherer;
  private int counter = 0;

  public Controller(PersistenceLayer pLayer) {
    this.persistence = pLayer;
  }

  public void collect(Map<String, Object> config) {
    this.gatherer = new FileSystemGatherer(config);

    int threads = (Integer) config.get(Constants.CNF_THREAD_COUNT);
    Map<String, Object> adaptorcnf = this.getAdaptorConfig(config);

    LOGGER.info("{} files to be processed for collection {}", gatherer.getCount(),
        config.get(Constants.CNF_COLLECTION_NAME));

    this.startJobs(threads, adaptorcnf);

  }

  private Map<String, Object> getAdaptorConfig(Map<String, Object> config) {
    final Map<String, Object> adaptorcnf = new HashMap<String, Object>();
    for (String key : config.keySet()) {
      if (key.startsWith("adaptor.")) {
        adaptorcnf.put(key, config.get(key));
      }
    }

    adaptorcnf.put(Constants.CNF_COLLECTION_ID, config.get(Constants.CNF_COLLECTION_NAME));
    return adaptorcnf;
  }

  private void startJobs(int threads, Map<String, Object> adaptorcnf) {
    this.pool = Executors.newFixedThreadPool(threads);
    List<ProcessingRule> rules = this.getRules();

    for (int i = 0; i < threads; i++) {
      final FITSAdaptor f = new FITSAdaptor();
      f.setController(this);
      f.setRules(rules);
      f.configure(adaptorcnf);

      this.pool.submit(f);
    }

    this.pool.shutdown();

    try {
      // What happens if the time out occurrs first?
      boolean terminated = this.pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

      if (terminated) {
        LOGGER.info("Gathering process finished successfully");
        String collection = (String) adaptorcnf.get(Constants.CNF_COLLECTION_ID);
        ActionLog log = new ActionLog(collection, ActionLog.UPDATED_ACTION);
        new ActionLogHelper(this.persistence).recordAction(log);
        
      } else {
        LOGGER.error("Time out occurred, gathering process was terminated");
      }

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public PersistenceLayer getPersistence() {
    return this.persistence;
  }

  public synchronized DigitalObjectStream getNext() {
    List<DigitalObjectStream> next = this.gatherer.getNext(1);
    DigitalObjectStream result = null;

    if (!next.isEmpty()) {
      result = next.get(0);
    }

    this.counter++;

    if (counter % 1000 == 0) {
      LOGGER.info("Finished processing {} files", counter);
    }

    return result;
  }

  // TODO this should be generated via some user input.
  private List<ProcessingRule> getRules() {
    List<ProcessingRule> rules = new ArrayList<ProcessingRule>();
    rules.add(new HtmlInfoProcessingRule());
    rules.add(new EmptyValueProcessingRule());
    rules.add(new FormatVersionResolutionRule());
    return rules;
  }

}
