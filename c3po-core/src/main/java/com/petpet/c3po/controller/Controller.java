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

/**
 * A simple controller that manages the operations coming as input from the
 * client applications. This class ties up the gathering, adaptation and
 * consolidation of data.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class Controller {

  /**
   * A default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

  /**
   * The persistence layer that this class uses.
   */
  private PersistenceLayer persistence;

  /**
   * A thread pool for the adaptors.
   */
  private ExecutorService adaptorPool;

  /**
   * A thread pool for the consolidators.
   */
  private ExecutorService consolidatorPool;

  /**
   * A meta data gatherer that collects meta data objects.
   */
  private MetaDataGatherer gatherer;

  /**
   * A processing queue that is passed to each adaptor and is used for the
   * synchronisation between adaptors and consolidators.
   */
  private final Queue<Element> processingQueue;

  private final Map<String, Class<? extends AbstractAdaptor>> knownAdaptors;

  /**
   * This constructors sets the persistence layer, initializes the processing
   * queue and the {@link LocalFileGatherer};
   * 
   * @param pLayer
   */
  public Controller(PersistenceLayer pLayer) {
    this.persistence = pLayer;
    this.processingQueue = new LinkedList<Element>();
    this.gatherer = new LocalFileGatherer();
    this.knownAdaptors = new HashMap<String, Class<? extends AbstractAdaptor>>();

    // TODO detect adaptors automatically from the class path
    // and add them to this map.
    this.knownAdaptors.put("FITS", FITSAdaptor.class);
    this.knownAdaptors.put("TIKA", TIKAAdaptor.class);
  }

  /**
   * This starts a gather-adapt-persist workflow, where all the needed
   * compontents are configured and run. If the passed configuration is invalid
   * an exception is thrown.
   * 
   * @param config
   *          a map of the application configurations.
   * @throws C3POConfigurationException
   *           if the configuration is missing or invalid.
   */
  public void processMetaData(Map<String, String> config) throws C3POConfigurationException {

    this.checkConfiguration(config);
    this.gatherer.setConfig(config);

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

    this.startWorkers(name, adaptorThreads, consThreads, type, adaptorcnf);

  }

  /**
   * Checks the config passed to this controller for required values.
   * 
   * @param config
   * @throws C3POConfigurationException
   */
  private void checkConfiguration(final Map<String, String> config) throws C3POConfigurationException {

    if (config == null) {
      throw new C3POConfigurationException("No config map provided");
    }

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

  /**
   * Filters out only adaptor specific configurations. This method returns a map
   * of configs with keys in the form 'c3po.adaptor.[rest]' or
   * 'c3po.adaptor.[prefix].[rest]', where rest is any arbitrary string and
   * prefix is the adaptor prefix returned in
   * {@link AbstractAdaptor#getAdaptorPrefix()}
   * 
   * @param config
   *          the config to filter.
   * @param prefix
   *          the prefix to look for.
   * @return a map with the adaptor specific configuration.
   */
  private Map<String, String> getAdaptorConfig(Map<String, String> config, String prefix) {
    final Map<String, String> adaptorcnf = new HashMap<String, String>();
    for (String key : config.keySet()) {
      if (key.startsWith("c3po.adaptor.") || key.startsWith("c3po.adaptor." + prefix.toLowerCase())) {
        adaptorcnf.put(key, config.get(key));
      }
    }

    return adaptorcnf;
  }

  /**
   * Starts all the workers. Including the adaptors, consolidators and gatherer.
   * 
   * @param collection
   *          the name of the collection that is processed.
   * @param adaptThreads
   *          the number of adaptor threads in the pool.
   * @param consThreads
   *          the number of consolidator threads in the pool.
   * @param type
   *          the type of the adaptors.
   * @param adaptorcnf
   *          the adaptor configuration.
   */
  private void startWorkers(String collection, int adaptThreads, int consThreads, String type,
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

  // TODO this should be generated via some user input.
  /**
   * Obtains a list of {@link ProcessingRule} objects for the adaptors.
   * 
   * @param name
   *          the name of the collection that is going to be processed.
   * @return the list of rules.
   */
  private List<ProcessingRule> getRules(String name) {
    List<ProcessingRule> rules = new ArrayList<ProcessingRule>();

    rules.add(new HtmlInfoProcessingRule());
    rules.add(new EmptyValueProcessingRule());
    rules.add(new FormatVersionResolutionRule());
    rules.add(new AssignCollectionToElementRule(name));

    return rules;
  }

  /**
   * Gets a new adaptor instance based on the type of adaptor. if the type is
   * unknown, then null is returned.
   * 
   * @param type
   *          the type of the adaptor.
   * @return the instance of the adaptor.
   */
  private AbstractAdaptor getAdaptor(String type) {
    AbstractAdaptor adaptor = null;
    Class<? extends AbstractAdaptor> clazz = this.knownAdaptors.get(type);
    if (clazz != null) {
      try {

        adaptor = clazz.newInstance();

      } catch (InstantiationException e) {
        LOG.error("An error occurred while instantiating the adaptor: ", e.getMessage());
      } catch (IllegalAccessException e) {
        LOG.error("An error occurred while instantiating the adaptor: ", e.getMessage());
      }
    }

    return adaptor;
  }

}
