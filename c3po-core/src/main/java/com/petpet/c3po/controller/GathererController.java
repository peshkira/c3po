package com.petpet.c3po.controller;

import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.adaptor.fits.FITSDigesterAdaptor;
import com.petpet.c3po.api.MetaDataGatherer;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.C3POConfig;
import com.petpet.c3po.datamodel.C3POConfig.GathererType;
import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.gatherer.FileSystemGatherer;
import com.petpet.c3po.utils.Helper;

public class GathererController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GathererController.class);

  private Date date;
  private DigitalCollection collection;
  private PersistenceLayer persitence;

  public GathererController(PersistenceLayer pLayer, DigitalCollection dc, Date d) {
    this.setPersitence(pLayer);
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

      if (gatherer.getCount() > 100) {
        int counter = 10;
        List<InputStream> next = gatherer.getNext(10);
        while (!next.isEmpty()) {
          LOGGER.debug("got next " + next.size());
          this.dispatch(next);
          next = gatherer.getNext(10);
          counter += 10;

          if (counter % 500 == 0) {
            LOGGER.info("Done {} files", counter);
            cleanUp();
          }
        }

      } else {
        List<InputStream> all = gatherer.getAll();
        this.dispatch(all);
        cleanUp();
      }

    }

  }
  
  private void cleanUp() {
    DigitalCollection tmp = (DigitalCollection) this.getPersitence().handleFindById(DigitalCollection.class, this.collection.getId());
    tmp.getElements().addAll(this.collection.getElements());
    this.collection = tmp;
    this.collection = (DigitalCollection) this.getPersitence().handleUpdate(DigitalCollection.class, this.collection);
    this.getPersitence().getEntityManager().detach(this.collection);
    this.collection.setElements(new HashSet<Element>());
    
//    this.collection = (DigitalCollection) this.getPersitence().handleUpdate(DigitalCollection.class, this.collection);
    
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
        throw new RuntimeException("Gatherer not supported yet");
    }
    return gatherer;
  }

  private void dispatch(List<InputStream> list) {
    FITSDigesterAdaptor fits = new FITSDigesterAdaptor(this);
    for (InputStream is : list) {
      fits.setStream(is);
      Element e = fits.getElement();

      this.processElement(e);
    }

  }

  public synchronized void processElement(Element e) {
    if (e != null) {
      e.setCollection(this.collection);
      Element stored = (Element) this.getPersitence().handleCreate(Element.class, e);
      if (stored != null) {
        this.collection.getElements().add(e);
      }
    }
  }

  public PersistenceLayer getPersitence() {
    return persitence;
  }

  public void setPersitence(PersistenceLayer persitence) {
    this.persitence = persitence;
  }

}
