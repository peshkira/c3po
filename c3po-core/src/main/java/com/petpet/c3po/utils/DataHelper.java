package com.petpet.c3po.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.ActionLog;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.datamodel.MetadataRecord;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.Property.PropertyType;
import com.petpet.c3po.datamodel.Source;

public final class DataHelper {

  private static final Logger LOG = LoggerFactory.getLogger(DataHelper.class);

  private static Properties TYPES;

  public static void init() {
    try {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("datatypes.properties");
      TYPES = new Properties();
      TYPES.load(in);
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getPropertyType(String key) {
    return TYPES.getProperty(key, "STRING");
  }

  /**
   * Parses the element from a db object returned by the db.
   * 
   * @param obj
   *          the object to parse.
   * @return the Element.
   */
  public static Element parseElement(final DBObject obj, final PersistenceLayer pl) {
    String coll = (String) obj.get("collection");
    String uid = (String) obj.get("uid");
    String name = (String) obj.get("name");

    Element e = new Element(coll, uid, name);
    e.setId(obj.get("_id").toString());
    e.setMetadata(new ArrayList<MetadataRecord>());

    DBObject meta = (BasicDBObject) obj.get("metadata");
    for (String key : meta.keySet()) {
      MetadataRecord rec = new MetadataRecord();
      DBObject prop = (DBObject) meta.get(key);
      Property p = pl.getCache().getProperty(key);
      rec.setProperty(p);
      rec.setStatus(prop.get("status").toString());

      Object value = prop.get("value");
      if (value != null) {
        rec.setValue(value.toString());
      }

      // because of boolean and other type conversions.
      List<?> tmp = (List) prop.get("values");
      if (tmp != null) {
        List<String> values = new ArrayList<String>();
        for (Object o : tmp) {
          values.add(o.toString());
        }
        rec.setValues(values);
      }

      List<String> src = (List<String>) prop.get("sources");
      if (src != null) {
        List<String> sources = new ArrayList<String>();
        for (String s : src) {
          DBObject next = pl.find(Constants.TBL_SOURCES, new BasicDBObject("_id", s), new BasicDBObject()).next();
          String source = (String) next.get("name") + " " + next.get("version");
          sources.add(source);
        }
        rec.setSources(sources);
      }

      e.getMetadata().add(rec);
    }

    return e;
  }

  public static Source parseSource(DBObject object) {
    String id = (String) object.get("_id");
    String name = (String) object.get("name");
    String version = (String) object.get("version");

    Source s = new Source();
    s.setId(id);
    s.setName(name);
    s.setVersion(version);

    return s;
  }

  public static Filter parseFilter(DBObject object) {
    String d = (String) object.get("descriminator");
    String c = (String) object.get("collection");
    String p = (String) object.get("property");
    String v = (String) object.get("value");

    Filter f = new Filter(c, p, v);
    f.setDescriminator(d);

    return f;
  }

  public static ActionLog parseActionLog(DBObject object) {
    String c = (String) object.get("collection");
    String a = (String) object.get("action");
    Date d = (Date) object.get("date");

    return new ActionLog(c, a, d);
  }

  public static BasicDBObject getFilterQuery(Filter filter) {
    PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    BasicDBObject ref = new BasicDBObject("descriminator", filter.getDescriminator());
    DBCursor cursor = pl.find(Constants.TBL_FILTERS, ref);

    BasicDBObject query = new BasicDBObject("collection", filter.getCollection());

    Filter tmp;
    while (cursor.hasNext()) {
      DBObject next = cursor.next();
      tmp = DataHelper.parseFilter(next);
      if (tmp.getValue() != null) {

        Property property = pl.getCache().getProperty(tmp.getProperty());

        if (tmp.getValue().equals("Unknown")) {
          query.put("metadata." + tmp.getProperty() + ".values", new BasicDBObject("$exists", false));
          query.put("metadata." + tmp.getProperty() + ".value", new BasicDBObject("$exists", false));

        } else if (property.getType().equals(PropertyType.DATE.toString())) {

          Calendar cal = Calendar.getInstance();
          cal.set(Integer.parseInt(tmp.getValue()), Calendar.JANUARY, 1);
          Date start = cal.getTime();
          cal.set(Integer.parseInt(tmp.getValue()), Calendar.DECEMBER, 31);
          Date end = cal.getTime();

          BasicDBObject date = new BasicDBObject();
          date.put("$lte", end);
          date.put("$gte", start);

          query.put("metadata." + tmp.getProperty() + ".value", date);

        } else if (tmp.getValue().equals("Conflicted")) {
          query.put("metadata." + tmp.getProperty() + ".status", MetadataRecord.Status.CONFLICT.toString());

        } else {
          query.put("metadata." + tmp.getProperty() + ".value", inferValue(tmp.getValue()));
        }
      }
    }

    return query;
  }

  private static Object inferValue(String value) {
    Object result = value;
    if (value.equalsIgnoreCase("true")) {
      result = new Boolean(true);
    }

    if (value.equalsIgnoreCase("false")) {
      result = new Boolean(false);
    }

    return result;
  }

  private DataHelper() {

  }
}
