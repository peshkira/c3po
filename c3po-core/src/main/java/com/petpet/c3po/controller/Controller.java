package com.petpet.c3po.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.adaptor.fits.FITSAdaptor;
import com.petpet.c3po.adaptor.rules.AssignCollectionToElementRule;
import com.petpet.c3po.adaptor.rules.EmptyValueProcessingRule;
import com.petpet.c3po.adaptor.rules.FormatVersionResolutionRule;
import com.petpet.c3po.adaptor.rules.HtmlInfoProcessingRule;
import com.petpet.c3po.adaptor.tika.TIKAAdaptor;
import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.adaptor.ProcessingRule;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.gatherer.MetaDataGatherer;
import com.petpet.c3po.api.model.ActionLog;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.MetadataStream;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.gatherer.LocalFileGatherer;
import com.petpet.c3po.utils.ActionLogHelper;
import com.petpet.c3po.utils.exceptions.C3POConfigurationException;

public class Controller {

  private static final Logger LOG = LoggerFactory.getLogger(Controller.class);
  private PersistenceLayer persistence;
  private ExecutorService adaptorPool;
  private ExecutorService consolidatorPool;
  private MetaDataGatherer gatherer;
  private final Queue<Element> processingQueue;
  private int counter = 0;

  public Controller(PersistenceLayer pLayer) {
    this.persistence = pLayer;
    this.processingQueue = new LinkedList<Element>();
  }

  public void collect(Map<String, String> config) throws C3POConfigurationException {

    this.checkConfiguration(config);

    this.gatherer = new LocalFileGatherer(config);

    String adaptorsCount = null;
    String consCount = null;
    int adaptorThreads = 4;
    int consThreads = 2;

    try {

      consCount = config.get(Constants.CNF_CONSOLIDATORS_COUNT);
      consThreads = Integer.parseInt(consCount);
      if (consThreads <= 0) {
        LOG.warn("The provided consolidators count config '{}' is negative. Using the default.", consCount);
        consThreads = 2;
      }

    } catch (NumberFormatException e) {
      LOG.warn("The provided consolidators count config '{}' is invalid. Using the default.", consCount);
    }

    try {

      adaptorsCount = config.get(Constants.CNF_ADAPTORS_COUNT);
      adaptorThreads = Integer.parseInt(adaptorsCount);
      if (adaptorThreads <= 0) {
        LOG.warn("The provided consolidators count config '{}' is negative. Using the default.", adaptorsCount);
        adaptorThreads = 4;
      }

    } catch (NumberFormatException e) {
      LOG.warn("The provided adaptors count config '{}' is invalid. Using the default.", adaptorsCount);
    }

    String name = config.get(Constants.OPT_COLLECTION_NAME);
    String type = (String) config.get(Constants.OPT_INPUT_TYPE);
    String prefix = this.getAdaptor(type).getAdaptorPrefix();
    Map<String, String> adaptorcnf = this.getAdaptorConfig(config, prefix);

    this.startJobs(name, adaptorThreads, consThreads, type, adaptorcnf);

  }

  /**
   * Checks the config passed to this controller for required values.
   * 
   * @param config
   * @throws C3POConfigurationException
   */
  private void checkConfiguration(final Map<String, String> config) throws C3POConfigurationException {
    String inputType = config.get(Constants.OPT_INPUT_TYPE);
    if (inputType == null || (!inputType.equals("TIKA") && !inputType.equals("FITS"))) {
      throw new C3POConfigurationException("No input type specified. Please use one of FITS or TIKA.");
    }

    String path = config.get(Constants.OPT_COLLECTION_LOCATION);
    if (path == null) {
      throw new C3POConfigurationException("No input file path provided. Please provide a path to the input files.");
    }

    String name = config.get(Constants.OPT_COLLECTION_NAME);
    if (name == null || name.equals("")) {
      throw new C3POConfigurationException("The name of the collection is not set. Please set a name.");
    }
  }

  private Map<String, String> getAdaptorConfig(Map<String, String> config, String prefix) {
    final Map<String, String> adaptorcnf = new HashMap<String, String>();
    for (String key : config.keySet()) {
      if (key.startsWith("c3po.adaptor.") || key.startsWith("c3po.adaptor." + prefix.toLowerCase())) {
        adaptorcnf.put(key, config.get(key));
      }
    }

    return adaptorcnf;
  }

  private void startJobs(String collection, int adaptThreads, int consThreads, String type,
      Map<String, String> adaptorcnf) {

    this.adaptorPool = Executors.newFixedThreadPool(adaptThreads);
    this.consolidatorPool = Executors.newFixedThreadPool(consThreads);

    List<Consolidator> consolidators = new ArrayList<Consolidator>();

    for (int i = 0; i < consThreads; i++) {
      Consolidator c = new Consolidator(this.processingQueue);
      consolidators.add(c);
      this.consolidatorPool.submit(c);
    }

    // no more consolidators can be added.
    this.consolidatorPool.shutdown();

    List<ProcessingRule> rules = this.getRules(collection);

    for (int i = 0; i < adaptThreads; i++) {
      AbstractAdaptor a = this.getAdaptor(type);

      a.setCache(this.persistence.getCache());
      a.setQueue(this.processingQueue);
      a.setGatherer(this.gatherer);
      a.setConfig(adaptorcnf);
      a.setRules(rules);
      a.configure();

      this.adaptorPool.submit(a);
    }

    // no more adaptors can be added.
    this.adaptorPool.shutdown();

    new Thread(this.gatherer, "MetadataGatherer");

    try {

      // kills the pool and all adaptor workers after a month;
      boolean adaptorsTerminated = this.adaptorPool.awaitTermination(2678400, TimeUnit.SECONDS);

      if (adaptorsTerminated) {
        for (Consolidator c : consolidators) {
          c.setRunning(false);
        }

        synchronized (processingQueue) {
          this.processingQueue.notifyAll();
        }

        this.consolidatorPool.awaitTermination(2678400, TimeUnit.SECONDS);

      } else {
        LOG.error("Time out occurred, process was terminated");
      }

    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ActionLog log = new ActionLog(collection, ActionLog.UPDATED_ACTION);
    new ActionLogHelper(this.persistence).recordAction(log);
  }

  public PersistenceLayer getPersistence() {
    return this.persistence;
  }

  @Deprecated
  public synchronized MetadataStream getNext() {
    List<MetadataStream> next = this.gatherer.getNext(1);
    MetadataStream result = null;

    if (!next.isEmpty()) {
      result = next.get(0);
    }

    this.counter++;

    if (counter % 1000 == 0) {
      LOG.info("Finished processing {} files", counter);
    }

    return result;
  }

  // TODO this should be generated via some user input.
  private List<ProcessingRule> getRules(String name) {
    List<ProcessingRule> rules = new ArrayList<ProcessingRule>();

    rules.add(new HtmlInfoProcessingRule());
    rules.add(new EmptyValueProcessingRule());
    rules.add(new FormatVersionResolutionRule());
    rules.add(new AssignCollectionToElementRule(name));

    return rules;
  }

  private AbstractAdaptor getAdaptor(String type) {
    if (type.equals("FITS")) {
      return new FITSAdaptor();
    } else if (type.equals("TIKA")) {
      return new TIKAAdaptor();
    } else {
      return null;
    }
  }

}
