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

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Configurator;

public class CSVExportCommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(CSVExportCommand.class);

  private Option[] options;

  private long time = -1L;

  public CSVExportCommand(Option[] options) {
    this.options = options;
  }

  @Override
  public void execute() {
    long start = System.currentTimeMillis();
    LOG.info("Starting csv export of all data");

    final Configurator configurator = Configurator.getDefaultConfigurator();
    final Map<String, String> dbconf = new HashMap<String, String>();
    dbconf.put("host", "localhost");
    dbconf.put("port", "27017");
    dbconf.put("db.name", "c3po");

    configurator.configure(dbconf);

    final PersistenceLayer pLayer = configurator.getPersistence();
    final DBCursor allprops = pLayer.findAll("properties");
    final List<Property> props = this.getProperties(allprops);
    final BasicDBObject query = this.buildMatrixQuery(props);
    final DBCursor cursor = pLayer.find("elements", null, query);

    this.export(props, cursor);

    long end = System.currentTimeMillis();
    this.time = end - start;
  }

  @Override
  public long getTime() {
    return this.time;
  }

  /**
   * replaces all comma ocurrences in the values with an empty string.
   * 
   * @param str
   *          the string to check
   * @return a new altered string or an empty string if the input was null.
   */
  private String replace(String str) {
    return (str == null) ? "" : str.replaceAll(",", "");
  }

  /**
   * Extracts {@link Property} objects from the given cursor and only sets the
   * id and the name field.
   * 
   * @param cursor
   *          the cursor to look for property objects.
   * @return a list of properties or an empty list.
   */
  private List<Property> getProperties(final DBCursor cursor) {
    final List<Property> result = new ArrayList<Property>();

    while (cursor.hasNext()) {
      final DBObject next = cursor.next();

      final String id = (String) next.get("_id");
      final String name = (String) next.get("name");

      if (id != null && name != null) {
        final Property p = new Property();
        p.setId(id);
        p.setName(name);

        result.add(p);
      }
    }

    return result;
  }

  /**
   * Builds a query that will select the values for the passed properties and
   * the uid out of each element.
   * 
   * @param props
   *          the properties to select
   * @return the query.
   */
  private BasicDBObject buildMatrixQuery(final List<Property> props) {
    final BasicDBObject query = new BasicDBObject();

    query.put("_id", null);
    query.put("uid", 1);

    for (Property p : props) {
      query.put(p.getId(), 1);
    }

    return query;
  }

  /**
   * Exports the data retrieved by the cursor to a sparse matrix view where each
   * column is a property and each row is an element with the values for the
   * corresponding property.
   * 
   * @param props
   *          the columns
   * @param matrix
   *          the values for each element.
   */
  private void export(final List<Property> props, final DBCursor matrix) {
    try {
      final FileWriter writer = new FileWriter(this.getOutputFile("output"));

      // build header of csv
      writer.append("uid, ");
      for (Property p : props) {
        writer.append(p.getName() + ", ");
      }
      writer.append("\n");

      // for all elements append the values in the correct column
      while (matrix.hasNext()) {
        final BasicDBObject next = (BasicDBObject) matrix.next();

        // first the uid
        writer.append(replace((String) next.get("uid")) + ", ");

        // then the properties
        for (Property p : props) {
          final DBObject rec = (DBObject) next.get(p.getId());

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
