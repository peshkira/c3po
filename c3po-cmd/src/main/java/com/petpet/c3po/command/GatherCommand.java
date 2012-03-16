package com.petpet.c3po.command;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.NoResultException;

import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.controller.GathererController;
import com.petpet.c3po.dao.LocalPersistenceLayer;
import com.petpet.c3po.datamodel.C3POConfig;
import com.petpet.c3po.datamodel.C3POConfig.GathererType;
import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.db.PreparedQueries;
import com.petpet.c3po.utils.Configurator;

public class GatherCommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(GatherCommand.class);

  private Option[] options;

  private LocalPersistenceLayer pLayer;

  private long time = -1L;

  public GatherCommand(Option[] o) {
    this.options = o;
  }

  @Override
  public void execute() {
    LOG.info("Starting meta data gathering command.");
    long start = System.currentTimeMillis();

    final Map<String, String> c = new HashMap<String, String>();
    c.put(C3POConfig.LOCATION, this.getMetaDataPath());
    c.put(C3POConfig.NAME, "LocalFileSystem Config");
    c.put(C3POConfig.RECURSIVE, this.isRecursive().toString());

    final C3POConfig conf = new C3POConfig();
    conf.setType(GathererType.FS);
    conf.setConfigs(c);

    this.pLayer = new LocalPersistenceLayer();
    final Configurator configurator = new Configurator(this.pLayer);
    configurator.configure();

    final String name = this.getCollectionName();
    final DigitalCollection collection = this.getCollection(name);
    collection.setConfigurations(new HashSet<C3POConfig>(Arrays.asList(conf)));
    this.pLayer.handleUpdate(DigitalCollection.class, collection);

    final GathererController controller = new GathererController(this.pLayer, collection, new Date());
    controller.collectMetaData();
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

  private DigitalCollection getCollection(final String name) {
    PreparedQueries pq = new PreparedQueries(this.pLayer.getEntityManager());

    DigitalCollection collection = null;
    try {
      collection = pq.getCollectionByName(name);
    } catch (NoResultException e) {
      // swallow
    }

    if (collection == null) {
      collection = new DigitalCollection(name);
      this.pLayer.handleCreate(DigitalCollection.class, collection);
    }

    return collection;

  }

  @Override
  public long getTime() {
    return this.time;
  }

}
