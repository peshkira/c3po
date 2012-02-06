package com.petpet.c3po.controller;

import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.adaptor.fits.FITSMetaDataAdaptor;
import com.petpet.c3po.api.MetaDataGatherer;
import com.petpet.c3po.datamodel.C3POConfig;
import com.petpet.c3po.datamodel.C3POConfig.GathererType;
import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.db.DBManager;
import com.petpet.c3po.gatherer.FileSystemGatherer;

public class GathererController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GathererController.class);

  private Date date;
  private DigitalCollection collection;
  private Queue<Element> queue;
  private QueueProcessor processor;
  private ExecutorService pool;

  public GathererController(DigitalCollection dc, Date d) {
    this.queue = new LinkedList<Element>();
//    this.pool = Executors.newFixedThreadPool(5);

    this.setCollection(dc);
    this.setDate(d);
  }

  public DigitalCollection getCollection() {
    return collection;
  }

  public void setCollection(DigitalCollection collection) {
    this.collection = collection;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  // do I need the collection reference here...?
  public void collectMetaData() {
    LOGGER.info("Looking up configurations for collection {}", this.collection.getName());
    Set<C3POConfig> configs = this.getCollection().getConfigurations();

    for (C3POConfig conf : configs) {
      LOGGER.info("Found matching gatherer, starting...");
      MetaDataGatherer gatherer = this.getGatherer(conf.getType(), conf.getConfigs());
//      processor = new QueueProcessor();
//      pool.execute(processor);

      if (gatherer.getCount() > 100) {
        List<InputStream> next = gatherer.getNext(100);
        while (!next.isEmpty()) {
          LOGGER.info("got next " + next.size());
          this.dispatch(next);
          next = gatherer.getNext(100);

        }

      } else {
        List<InputStream> all = gatherer.getAll();
        this.dispatch(all);
      }

    }

  }

  private MetaDataGatherer getGatherer(GathererType type, Map<String, String> config) {
    MetaDataGatherer gatherer = null;

    switch (type) {
      case DEFAULT:
        throw new RuntimeException("No Gatherer selected");
      case FS:
        gatherer = new FileSystemGatherer(config);
        break;
      case SSH:
      case RODA:
      case ROSETTA:
      case ESD:
        break;
    }
    return gatherer;
  }

  private void dispatch(List<InputStream> list) {
    for (InputStream is : list) {
      FITSMetaDataAdaptor fits = new FITSMetaDataAdaptor(this, is);
      Element e = fits.extractMetaData();
      this.processElement(e);
//      this.pool.execute(fits);
    }

  }

  public synchronized void processElement(Element e) {
//    LOGGER.info("adding element {} to queue", e.getName());
//    this.queue.add(e);
    DBManager.getInstance().persist(e);
  }

  private class QueueProcessor extends Thread {

    private boolean running;

    public void run() {
      setRunning(true);

      while (isRunning()) {
        while (!queue.isEmpty()) {

          Element poll = queue.poll();
          DBManager.getInstance().persist(poll); // null check in persist
        }
      }

      LOGGER.info("Stopping Queue processing");
    }

    public boolean isRunning() {
      return running;
    }

    public void setRunning(boolean running) {
      this.running = running;
    }
  }

}
