package com.petpet.c3po.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.adaptor.fits.FITSDigesterAdaptor;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.MetadataRecord;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.gatherer.FileSystemGatherer;
import com.petpet.c3po.utils.DataHelper;

public class Controller {

  private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
  private PersistenceLayer persistence;
  private ExecutorService pool;
  private FileSystemGatherer gatherer;
  private int counter = 0;
  private String collection;
  private boolean infer = false;

  public Controller(PersistenceLayer pLayer) {
    this.persistence = pLayer;
  }

  public void collect(Map<String, Object> config) {
    this.gatherer = new FileSystemGatherer(config);
    this.collection = (String) config.get(Constants.CNF_COLLECTION_NAME);
    this.infer = (Boolean) config.get(Constants.CNF_INFER_DATE);

    LOGGER.info("{} files to be processed for collection {}", gatherer.getCount(), collection);

    this.startJobs((Integer) config.get(Constants.CNF_THREAD_COUNT));

  }

  private void startJobs(int threads) {
    this.pool = Executors.newFixedThreadPool(threads);

    for (int i = 0; i < threads; i++) {
      FITSDigesterAdaptor f = new FITSDigesterAdaptor(this);
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

  public Cache getCache() {
    return this.persistence.getCache();
  }

  public String getCollection() {
    return this.collection;
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
