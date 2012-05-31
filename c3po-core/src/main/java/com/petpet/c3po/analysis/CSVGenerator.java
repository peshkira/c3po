package com.petpet.c3po.analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Property;

public class CSVGenerator {

  private PersistenceLayer persistence;

  public CSVGenerator(PersistenceLayer p) {
    this.persistence = p;
  }

  public DBCursor buildMatrix(String collection) {
    final BasicDBObject ref = new BasicDBObject("collection", collection);
    final BasicDBObject query = new BasicDBObject();

    query.put("_id", null);
    query.put("uid", 1);
    query.put("metadata", 1);

    return this.persistence.find("elements", ref, query);
  }

  public DBCursor buildMatrix(String collection, String mimetype) {
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
  public void export(final List<Property> props, final DBCursor matrix, String output) {
    try {
      final FileWriter writer = new FileWriter(output);

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
          if (value != null) {
            Object v = value.get("value");
            String val = (v == null) ? "" : replace(v.toString());
            writer.append(val);
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

  // /**
  // * Builds a query that will select the values for the passed properties and
  // * the uid out of each element.
  // *
  // * @param props
  // * the properties to select
  // * @return the query.
  // */
  // private BasicDBObject buildMatrixQuery(final String mime, final
  // List<Property> props) {
  //
  // final Property mimetype =
  // this.persistence.getCache().getProperty("mimetype");
  // final BasicDBObject query = new BasicDBObject();
  //
  // // final BasicDBObject partition
  //
  // // BasicDBObjectBuilder.start("$and", )
  //
  // query.put("_id", null);
  // query.put("uid", 1);
  // query.put("metadata.key", 1);
  // query.put("metadata.value", 1);
  //
  // return query;
  // }

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
