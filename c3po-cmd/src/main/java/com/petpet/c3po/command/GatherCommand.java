package com.petpet.c3po.command;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.controller.Controller;
import com.petpet.c3po.dao.LocalPersistenceLayer;
import com.petpet.c3po.utils.Configurator;

public class GatherCommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(GatherCommand.class);

  private Option[] options;

  private PersistenceLayer pLayer;

  private long time = -1L;

  public GatherCommand(Option[] o) {
    this.options = o;
  }

  @Override
  public void execute() {
    LOG.info("Starting meta data gathering command.");
    long start = System.currentTimeMillis();
    
    final Map<String, String> dbconf = new HashMap<String, String>();
    dbconf.put("host", "localhost");
    dbconf.put("port", "27017");
    dbconf.put("db.name", "c3po");
   
    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure(dbconf);
    this.pLayer = configurator.getPersistence();

    final Map<String, String> conf = new HashMap<String, String>();
    conf.put("config.location", this.getMetaDataPath());
    conf.put("config.recursive", this.isRecursive().toString());
    conf.put("config.threads", "10");
    
    final Controller ctrl = new Controller(this.pLayer);
    ctrl.collect(conf);
    
    this.pLayer.close();
    long end = System.currentTimeMillis();
    this.time = end - start;
  }

  private String getMetaDataPath() {
    for (Option o : this.options) {
      if (o.getArgName().equals(CommandConstants.GATHER_DIR_ARGUMENT)) {
        return o.getValue();
      }
    }

    return null;
  }

  private Boolean isRecursive() {
    for (Option o : this.options) {
      if (o.getLongOpt().equals(CommandConstants.RECURSIVE_OPTION)) {
        return true;
      }
    }

    return false;
  }

  private String getCollectionName() {
    for (Option o : this.options) {
      if (o.getArgName().equals(CommandConstants.COLLECTION_ID_ARGUMENT)) {
        return o.getValue();
      }
    }

    LOG.warn("No collection identifier found, using DefaultCollection");
    return "DefaultCollection";
  }

//  private DigitalCollection getCollection(final String name) {
//    PreparedQueries pq = new PreparedQueries(this.pLayer.getEntityManager());
//
//    DigitalCollection collection = null;
//    try {
//      collection = pq.getCollectionByName(name);
//    } catch (NoResultException e) {
//      // swallow
//    }
//
//    if (collection == null) {
//      collection = new DigitalCollection(name);
//      this.pLayer.handleCreate(DigitalCollection.class, collection);
//    }
//
//    return collection;
//
//  }

  @Override
  public long getTime() {
    return this.time;
  }

}
