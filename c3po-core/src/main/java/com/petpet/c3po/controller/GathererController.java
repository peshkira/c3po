package com.petpet.c3po.controller;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.adaptor.fits.FITSMetaDataAdaptor;
import com.petpet.c3po.api.MetaDataGatherer;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.C3POConfig;
import com.petpet.c3po.datamodel.C3POConfig.GathererType;
import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.gatherer.FileSystemGatherer;

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
        throw new RuntimeException("Gatherer not supported yet");
    }
    return gatherer;
  }

  private void dispatch(List<InputStream> list) {
    for (InputStream is : list) {
      FITSMetaDataAdaptor fits = new FITSMetaDataAdaptor(this, is);
      Element e = fits.extractMetaData();
      this.processElement(e);
    }

  }
  
  public synchronized void processElement(Element e) {
    this.getPersitence().handleCreate(Element.class, e);
  }

  public PersistenceLayer getPersitence() {
    return persitence;
  }

  public void setPersitence(PersistenceLayer persitence) {
    this.persitence = persitence;
  }

}
