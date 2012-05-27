package com.petpet.c3po.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.analysis.CSVGenerator;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Configurator;

public class CSVExportCommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(CSVExportCommand.class);

  private Option[] options;

  private long time = -1L;

  private CSVGenerator generator;

  public CSVExportCommand(Option[] options) {
    this.options = options;

  }

  @Override
  public void execute() {
    long start = System.currentTimeMillis();
    LOG.info("Starting csv export of all data");

    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();

    final PersistenceLayer pLayer = configurator.getPersistence();
    this.generator = new CSVGenerator(pLayer);
    
    final DBCursor cursor= this.generator.buildMatrix(this.getCollectionName());
    final DBCursor allprops = pLayer.findAll("properties");
    final List<Property> props = this.getProperties(allprops);

    this.generator.export(props, cursor, this.getOutputFile("matrix"));

    long end = System.currentTimeMillis();
    this.time = end - start;
  }

  @Override
  public long getTime() {
    return this.time;
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
      if (o.getArgName().equals(CommandConstants.EXPORT_OUTPUT_PATH)) {
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
