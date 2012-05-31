package com.petpet.c3po.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.adaptor.fits.FITSAdaptor;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.gatherer.FileSystemGatherer;

public class Controller {

  private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
  private PersistenceLayer persistence;
  private ExecutorService pool;
  private FileSystemGatherer gatherer;
  private int counter = 0;
  private String collection;
  private Map<String, Object> adaptorConfig;

  public Controller(PersistenceLayer pLayer) {
    this.persistence = pLayer;
  }

  public void collect(Map<String, Object> config) {
    this.gatherer = new FileSystemGatherer(config);
    this.collection = (String) config.get(Constants.CNF_COLLECTION_NAME);
    this.adaptorConfig = this.getAdaptorConfig(config);

    LOGGER.info("{} files to be processed for collection {}", gatherer.getCount(), collection);

    this.startJobs((Integer) config.get(Constants.CNF_THREAD_COUNT));

  }

  private Map<String, Object> getAdaptorConfig(Map<String, Object> config) {
    final Map<String, Object> adaptorcnf = new HashMap<String, Object>();
    for (String key : config.keySet()) {
      if (key.startsWith("adaptor.")) {
        adaptorcnf.put(key, config.get(key));
      }
    }

    adaptorcnf.put(Constants.CNF_COLLECTION_ID, this.collection);
    return adaptorcnf;
  }

  private void startJobs(int threads) {
    this.pool = Executors.newFixedThreadPool(threads);

    for (int i = 0; i < threads; i++) {
      final FITSAdaptor f = new FITSAdaptor();
      f.setController(this);
      f.setConfig(this.adaptorConfig);

      this.pool.submit(f);
    }

    this.pool.shutdown();

    try {
      // What happens if the time out occurrs first?
      this.pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public PersistenceLayer getPersistence() {
    return this.persistence;
  }

  public synchronized String getNext() {
    List<String> next = this.gatherer.getNext(1);
    String result = null;
    if (next.isEmpty()) {
      LOGGER.info("Gathering process finished");

    } else {
      result = next.get(0);
    }

    this.counter++;

    if (counter % 500 == 0) {
      LOGGER.info("Finished processing {} files", counter);
    }

    return result;
  }

}
