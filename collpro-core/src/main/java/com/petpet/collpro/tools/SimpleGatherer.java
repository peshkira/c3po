package com.petpet.collpro.tools;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.api.ITool;
import com.petpet.collpro.api.Notification;
import com.petpet.collpro.api.NotificationListener;
import com.petpet.collpro.api.utils.ConfigurationException;
import com.petpet.collpro.common.Config;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.db.DBManager;

public class SimpleGatherer implements NotificationListener {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleGatherer.class);

  private ITool converter;

  private DigitalCollection collection;

  public SimpleGatherer(ITool converter, DigitalCollection collection) {
    this.converter = converter;

    if (collection == null) {
      this.collection = new DigitalCollection("collection-" + new Date().toString());
    } else {
      this.collection = collection;
    }

    DBManager.getInstance().persist(this.collection);
  }

  public void gather(File dir) {
    if (dir == null || !dir.isDirectory()) {
      LOG.warn("Provided folder is null or not a folder");
    }

    Map<String, Object> config = new HashMap<String, Object>();
    config.put(Config.DATE_CONF, new Date());
    config.put(Config.COLLECTION_CONF, this.collection);
    config.put(Config.FITS_FILES_CONF, dir.listFiles(new XMLFileFilter()));

    try {
      this.converter.addObserver(this);
      this.converter.configure(config);
      this.converter.execute();
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }

  }

  private class XMLFileFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
      return pathname.getName().endsWith(".xml");
    }

  }

  @Override
  public void notify(Notification<?> n) {
    Object o = n.getData();
    if (o != null && n.getClazz().equals(Element.class)) {
      DBManager.getInstance().persist(o);
    }

  }

}
