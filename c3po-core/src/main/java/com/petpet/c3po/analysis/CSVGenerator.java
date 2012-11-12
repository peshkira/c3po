package com.petpet.c3po.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.DataHelper;

public class CSVGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(CSVGenerator.class);

  private PersistenceLayer persistence;

  public CSVGenerator(PersistenceLayer p) {
    this.persistence = p;
  }

  /**
   * Exports all the data from the given collection to a sparse matrix view
   * where each column is a property and each row is an element with the values
   * for the corresponding property.
   * 
   * @param collection
   *          the collection to export
   * @param output
   *          the output file
   */
  public void exportAll(String collection, String output) {
    final DBCursor matrix = this.buildMatrix(collection);
    final DBCursor allprops = this.persistence.findAll(Constants.TBL_PROEPRTIES);
    final List<Property> props = this.getProperties(allprops);

    this.write(matrix, props, output);
  }

  /**
   * Exports all data for the given mimetype to a sparse matrix view where each
   * column is a property and each row is an element with the values for the
   * corresponding property.
   * 
   * @param collection
   *          the collection to export
   * @param mimetype
   *          the mimetype to filter
   * @param output
   *          the output file
   */
  @Deprecated
  public void exportAll(String collection, String mimetype, String output) {
    final DBCursor matrix = this.buildMatrix(collection, mimetype);
    final DBCursor allprops = this.persistence.findAll(Constants.TBL_PROEPRTIES);
    final List<Property> props = this.getProperties(allprops);

    this.write(matrix, props, output);
  }

  /**
   * Exports all the given properties for the given mimetype to a sparse matrix
   * view where each column is a property and each row is an element with the
   * values for the corresponding property.
   * 
   * @param collection
   *          the collection to export
   * @param props
   *          the properties filter
   * @param output
   *          the output file.
   */
  public void export(final String collection, final List<Property> props, String output) {
    final DBCursor matrix = this.buildMatrix(collection, props);

    this.write(matrix, props, output);
  }

  /**
   * Exports all the given properties for the given mimetype to a sparse matrix
   * view where each column is a property and each row is an element with the
   * values for the corresponding property.
   * 
   * @param collection
   *          the collection to export
   * @param mimetype
   *          the mimetype filter
   * @param props
   *          the properties filter
   * @param output
   *          the output file
   */
  @Deprecated
  public void export(final String collection, final String mimetype, final List<Property> props, String output) {
    final DBCursor matrix = this.buildMatrix(collection, mimetype, props);

    this.write(matrix, props, output);
  }

  public void export(final Filter filter, String output) {
    final DBCursor allprops = this.persistence.findAll(Constants.TBL_PROEPRTIES);
    final List<Property> props = this.getProperties(allprops);

    this.export(filter, props, output);
  }

  public void export(final Filter filter, final List<Property> props, String output) {
    BasicDBObject query = DataHelper.getFilterQuery(filter);
    DBCursor matrix = this.persistence.find(Constants.TBL_ELEMENTS, query);

    this.write(matrix, props, output);
  }

  private void write(DBCursor matrix, List<Property> props, String output) {
    try {

      final File file = new File(output);

      LOG.info("Will export data in {}", file.getAbsolutePath());

      if (file.getParentFile() != null && !file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }

      file.createNewFile();

      final FileWriter writer = new FileWriter(file);

      // build header of csv
      writer.append("uid, ");
      for (Property p : props) {
        writer.append(p.getKey() + ", ");
      }
      writer.append("\n");

      // for all elements append the values in the correct column
      while (matrix.hasNext()) {
        final BasicDBObject next = (BasicDBObject) matrix.next();

        // first the uid
        writer.append(replace((String) next.get("uid")) + ", ");

        final BasicDBObject metadata = (BasicDBObject) next.get("metadata");
        // then the properties
        for (Property p : props) {
          final BasicDBObject value = (BasicDBObject) metadata.get(p.getId());
          final String val = this.getValueFromMetaDataRecord(value);
          writer.append(val);
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

  private DBCursor buildMatrix(final String collection) {
    final BasicDBObject ref = new BasicDBObject("collection", collection);
    final BasicDBObject query = new BasicDBObject();

    query.put("_id", null);
    query.put("uid", 1);
    query.put("metadata", 1);

    return this.persistence.find("elements", ref, query);
  }

  @Deprecated
  private DBCursor buildMatrix(final String collection, final String mimetype) {
    final BasicDBObject ref = new BasicDBObject("collection", collection);
    final BasicDBObject query = new BasicDBObject();
    final List<BasicDBObject> refs = new ArrayList<BasicDBObject>();
    final Property m = this.persistence.getCache().getProperty("mimetype");

    refs.add(new BasicDBObject("metadata." + m.getId() + ".value", mimetype));

    ref.put("$and", refs);

    query.put("_id", null);
    query.put("uid", 1);
    query.put("metadata", 1);

    return this.persistence.find("elements", ref, query);

  }

  private DBCursor buildMatrix(final String collection, List<Property> props) {
    final BasicDBObject ref = new BasicDBObject("collection", collection);
    final BasicDBObject query = new BasicDBObject();

    query.put("_id", null);
    query.put("uid", 1);
    query.put("metadata", 1);

    for (Property p : props) {
      query.put("metadata." + p.getId(), 1);
    }

    return this.persistence.find(Constants.TBL_ELEMENTS, ref, query);
  }

  /**
   * Builds a query that will select the values for the passed properties and
   * the uid out of each element.
   * 
   * @param props
   *          the properties to select
   * @return the query.
   */
  private DBCursor buildMatrix(final String collection, final String mimetype, final List<Property> props) {
    final BasicDBObject ref = new BasicDBObject("collection", collection);
    final BasicDBObject query = new BasicDBObject();
    final List<BasicDBObject> refs = new ArrayList<BasicDBObject>();
    final Property m = this.persistence.getCache().getProperty("mimetype");

    refs.add(new BasicDBObject("metadata." + m.getId() + ".value", mimetype));

    ref.put("$and", refs);

    query.put("_id", null);
    query.put("uid", 1);
    query.put("metadata", 1);

    for (Property p : props) {
      query.put("metadata." + p.getId(), 1);
    }

    return this.persistence.find(Constants.TBL_ELEMENTS, ref, query);
  }

  private String getValueFromMetaDataRecord(final BasicDBObject value) {
    String result = "";
    if (value != null) {
      final Object v = value.get("value");
      result = (v == null) ? "CONFLICT" : replace(v.toString());
    }

    return result;
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
      final String name = (String) next.get("key");

      if (id != null && name != null) {
        final Property p = new Property();
        p.setId(id);
        p.setKey(name);

        result.add(p);
      }
    }

    return result;
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

}
