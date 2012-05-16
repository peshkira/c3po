package com.petpet.c3po.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Configurator;

public class CSVExportCommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(CSVExportCommand.class);

  private Option[] options;

  private PersistenceLayer pLayer;

  private long time = -1L;

  public CSVExportCommand(Option[] options) {
    this.options = options;
  }

  @Override
  public void execute() {
    long start = System.currentTimeMillis();

    final Map<String, String> dbconf = new HashMap<String, String>();
    dbconf.put("host", "localhost");
    dbconf.put("port", "27017");
    dbconf.put("db.name", "c3po");

    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure(dbconf);
    this.pLayer = configurator.getPersistence();

    Cache cache = this.pLayer.getCache();
    DB db = this.pLayer.getDB();
    DBCollection elements = db.getCollection("elements");

    DBCollection properties = db.getCollection("properties");

    DBCursor allprops = properties.find();
    List<Property> props = new ArrayList<Property>();

    while (allprops.hasNext()) {
      DBObject next = allprops.next();

      String id = (String) next.get("_id");
      String name = (String) next.get("name");
      Property p = new Property();
      p.setId(id);
      p.setName(name);
      props.add(p);
    }

    BasicDBObject query = new BasicDBObject();

    query.put("_id", null);
    query.put("uid", 1);

    for (Property p : props) {
      query.put(p.getId(), 1);
    }

    DBCursor cursor = elements.find(null, query);

    try {
      FileWriter writer = new FileWriter(this.getOutputFile("output"));

      writer.append("uid, ");
      for (Property p : props) {
        writer.append(p.getName() + ", ");
      }
      writer.append("\n");

      while (cursor.hasNext()) {
        BasicDBObject next = (BasicDBObject) cursor.next();

        // System.out.println(next);
        writer.append(replace((String) next.get("uid")) + ", ");

        for (Property p : props) {
          // System.out.println("value: " + next.get(p.getId()));
          DBObject rec = (DBObject) next.get(p.getId());
          // System.out.println("value: " + rec.get("value"));
          if (rec != null) {
            writer.append(replace((String) rec.get("value")));
          }
          
          writer.append(", ");
        }

        writer.append("\n");
      }

      writer.flush();
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    long end = System.currentTimeMillis();
    this.time = end - start;
  }

  @Override
  public long getTime() {
    return this.time;
  }

  private static String replace(String str) {
    return (str == null) ? "" : str.replaceAll(",", "");
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

  private String getOutputFile(String name) {
    final String extension = ".csv";
    String result = null;

    for (Option o : this.options) {
      if (o.getArgName().equals(CommandConstants.PROFILE_FILENAME_ARGUMENT)) {
        result = o.getValue();
      }
    }

    if (result != null) {
      return result + File.separator + name + extension;
    }

    LOG.debug("No output filepath was specified, using default");
    return name + extension;
  }

}
