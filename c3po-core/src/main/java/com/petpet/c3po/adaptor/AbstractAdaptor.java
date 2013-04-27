package com.petpet.c3po.adaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.petpet.c3po.adaptor.rules.PostProcessingRule;
import com.petpet.c3po.adaptor.rules.PreProcessingRule;
import com.petpet.c3po.adaptor.rules.ProcessingRule;
import com.petpet.c3po.api.MetaDataGatherer;
import com.petpet.c3po.api.dao.ReadOnlyCache;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataStream;
import com.petpet.c3po.controller.Controller;

//TODO reorder methods
//TODO remove deprecated.
/**
 * The abstract adaptor class provides an encapsulation of a meta data adaptor.
 * This class has to be extended by any adaptor that maps any kind of objects
 * meta data to the internal model of C3PO. The implementor has to overwrite the
 * abstract methods and to provide the ability to parse the data coming from an
 * input stream to an {@link Element}.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public abstract class AbstractAdaptor implements Runnable {

  @Deprecated
  public static final String UNKNOWN_COLLECTION_ID = "unknown";

  @Deprecated
  private Controller controller;

  /**
   * The gatherer that is used to obtain the next meta data stream.
   */
  private MetaDataGatherer gatherer;

  /**
   * The configuration that is passed to this adaptor before invoking it.
   */
  private Map<String, String> config;

  /**
   * The processing rules that are passed to this adaptor before invoking it.
   */
  private List<ProcessingRule> rules;

  /**
   * A read only instance to the applications cache, so that the adaptor can
   * obtain {@link Property} and {@link Source} objects.
   */
  private ReadOnlyCache cache;

  /**
   * An internally managed queue that will store the parsed Elements for further
   * processing by the application.
   */
  private Queue<Element> elementsQueue;

  @Deprecated
  protected Controller getController() {
    return this.controller;
  }

  @Deprecated
  public void setController(Controller controller) {
    this.controller = controller;
  }

  /**
   * The prefix that this adaptor should be associated with. This method will be
   * used in order to determine which configurations should be passed to this
   * adaptor.
   * 
   * @return
   */
  public abstract String getAdaptorPrefix();

  /**
   * This method will be called once before the adaptor is started and gives the
   * implementing class a chance to configure itself. The implementor should
   * make use of the getConfig methods to read the values for the expected
   * configuration keys of the adaptor. The internally managed config map will
   * contain all configuration keys starting with the prefix 'c3po.adaptor' and
   * all configuration keys starting with the prefix 'c3po.adaptor.[prefix]',
   * where [prefix] is the value retunerd by
   * {@link AbstractAdaptor#getAdaptorPrefix()} of this class.
   * 
   */
  public abstract void configure();

  /**
   * This element is responsible for adapting the input stream in the
   * {@link MetadataStream#getData()} object to a {@link Element}.
   * 
   * @param ms
   *          the {@link MetadataStream} to adapt.
   * @return the parsed {@link Element} object.
   */
  public abstract Element parseElement(MetadataStream ms);

  /**
   * Sets the cache to the passed {@link ReadOnlyCache} iff it is not null.
   * 
   * @param cache
   *          the cache to set.
   */
  public final void setCache(ReadOnlyCache cache) {
    if (cache != null) {
      this.cache = cache;
    }
  }

  /**
   * Sets the gatherer to the passed {@link MetaDataGatherer} iff it is not
   * null.
   * 
   * @param gatherer
   *          the gatherer to set.
   */
  public final void setGatherer(MetaDataGatherer gatherer) {
    if (gatherer != null) {
      this.gatherer = gatherer;
    }
  }

  /**
   * Starts an infinite loop that runs this thread. The thread checks if the
   * gatherer has a next stream to process. If no, then it sleeps until it is
   * notified by the gatherer. If ye, then if gets the next stream and calls the
   * {@link AbstractAdaptor#parseElement(MetadataStream)} method. Then the
   * element is submitted for further processing.
   * 
   */
  @Override
  public final void run() {
    while (true) {
      try {

        // TODO add some other method
        // to check if the gatherer is finished
        // in order to decide when to break the loop
        synchronized (gatherer) {
          while (!gatherer.hasNext()) {
            gatherer.wait();
          }

          MetadataStream stream = this.gatherer.getNext();
          if (stream != null) {
            Element element = parseElement(stream);
            this.submitElement(element);
          }

        }

      } catch (InterruptedException e) {
        // LOG.warn("An error occurred in {}: {}", getName(), e.getMessage());
        break;
      }
    }
  }

  /**
   * Sets the processing rules to the given rules iff they are not null.
   * 
   * @param rules
   *          the list of processing rules.
   */
  public final void setRules(List<ProcessingRule> rules) {
    if (rules != null) {
      this.rules = rules;
    }
  }

  /**
   * Sets the configuration of this adaptor iff the given config is not null.
   * 
   * @param config
   *          the config to set.
   */
  public final void setConfig(Map<String, String> config) {
    if (config != null) {
      this.config = config;
    }
  }

  /**
   * Sets the elements queue to the given queue, iff it is not null.
   * 
   * @param q
   *          the queue to use.
   */
  public final void setQueue(Queue<Element> q) {
    if (q != null) {
      this.elementsQueue = q;
    }
  }

  /**
   * Gets a read only instance of the application cache. Can be used to obtain
   * {@link Property} and {@link Source} objects.
   * 
   * @return the {@link ReadOnlyCache}
   */
  protected ReadOnlyCache getCache() {
    return cache;
  }

  /**
   * Gets an unmodifiable list of {@link ProcessingRule} objects. All rules are
   * sorted by priority, where 0 is lowest and 1000 is highest.
   * 
   * @return the list of rules.
   */
  public final List<ProcessingRule> getRules() {
    if (this.rules != null) {

      Collections.sort(this.rules, new Comparator<ProcessingRule>() {

        // sorts from descending
        @Override
        public int compare(ProcessingRule r1, ProcessingRule r2) {
          int first = this.fixPriority(r2.getPriority());
          int second = this.fixPriority(r1.getPriority());
          return new Integer(first).compareTo(new Integer(second));
        }

        private int fixPriority(int prio) {
          if (prio < 0)
            return 0;

          if (prio > 1000)
            return 1000;

          return prio;
        }

      });

    }

    return Collections.unmodifiableList(rules);
  }

  /**
   * Gets a unmodifiable list of {@link PreProcessingRule} objects. They are
   * sorted by priority, where 0 is the lowest and 1000 is the highest.
   * 
   * @return the rules.
   */
  public final List<PreProcessingRule> getPreProcessingRules() {
    List<ProcessingRule> all = this.getRules();
    List<PreProcessingRule> prerules = new ArrayList<PreProcessingRule>();

    for (ProcessingRule rule : all) {
      if (rule instanceof PreProcessingRule) {
        prerules.add((PreProcessingRule) rule);
      }
    }

    return Collections.unmodifiableList(prerules);
  }

  /**
   * Gets a unmodifiable list of {@link PostProcessingRule} objects. They are
   * sorted by priority, where 0 is the lowest and 1000 is the highest.
   * 
   * @return the rules.
   */
  public final List<PostProcessingRule> getPostProcessingRules() {
    List<ProcessingRule> all = this.getRules();
    List<PostProcessingRule> prerules = new ArrayList<PostProcessingRule>();

    for (ProcessingRule rule : all) {
      if (rule instanceof PostProcessingRule) {
        prerules.add((PostProcessingRule) rule);
      }
    }

    return Collections.unmodifiableList(prerules);
  }

  /**
   * Gets a string config for the given key. If there is no value associated
   * with that key, then an empty string is returned.
   * 
   * @param key
   *          the key to look for.
   * @return the value for the given key, or an empty string if nothing was
   *         associated with it.
   */
  protected String getConfig(String key) {
    return this.getStringConfig(key, "");
  }

  /**
   * Gets a string config for the given key. If there is no value associated
   * with that key, then the given defaultValue string is returned.
   * 
   * @param key
   *          the key to look for.
   * @param defaultValue
   *          the value to return if nothing is associated with the given key.
   * @return the value for the given key, or the given defaultValue string if
   *         nothing was associated with it.
   */
  protected String getStringConfig(String key, String defaultValue) {
    String result = null;
    if (this.config != null) {
      result = (String) this.config.get(key);
    }

    if (result == null) {
      result = defaultValue;
    }

    return result;
  }

  /**
   * Gets a boolean config for the given key. If there is no value associated
   * with that key, then the given defaultValue is returned.
   * 
   * @param key
   *          the key to look for.
   * @param defaultValue
   *          the value to return if nothing is associated with the given key.
   * @return the value for the given key, or the given defaultValue if nothing
   *         was associated with it.
   */
  protected Boolean getBooleanConfig(String key, boolean defaultValue) {
    Boolean result = null;
    if (this.config != null) {
      String val = this.config.get(key);
      if (val != null && !val.equals("")) {
        result = Boolean.parseBoolean(val);
      }
    }

    if (result == null) {
      result = defaultValue;
    }

    return result;
  }

  /**
   * Gets a integer config for the given key. If there is no value associated
   * with that key or if it is not a valid integer, then the given defaultValue
   * int is returned.
   * 
   * @param key
   *          the key to look for.
   * @param defaultValue
   *          the value to return if nothing is associated with the given key.
   * @return the value for the given key, or the given defaultValue if nothing
   *         was associated with it.
   */
  protected Integer getIntegerConfig(String key, int defaultValue) {
    Integer result = null;
    if (this.config != null) {
      String val = this.config.get(key);
      if (val != null && !val.equals("")) {
        try {
          result = Integer.parseInt(val);
        } catch (NumberFormatException e) {
          // do nothing
        }
      }
    }

    if (result == null) {
      result = defaultValue;
    }

    return result;
  }

  /**
   * Submits the passed element to the processing queue iff it is not null. This
   * method is synchronized on the queue.
   * 
   * @param e
   *          the element to submit to the queue.
   */
  private final void submitElement(Element e) {
    synchronized (elementsQueue) {
      if (e != null) {
        elementsQueue.add(e);
        elementsQueue.notify();
      }
    }
  }

}
