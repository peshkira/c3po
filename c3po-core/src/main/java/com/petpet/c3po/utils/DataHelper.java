package com.petpet.c3po.utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.MetadataRecord;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.Property.PropertyType;
import com.petpet.c3po.datamodel.Source;

public final class DataHelper {

  private static final Logger LOG = LoggerFactory.getLogger(DataHelper.class);

  private static Properties TYPES;

  private static final String[] PATTERNS = { "yyyy:MM:dd HH:mm:ss", "MM/dd/yyyy HH:mm:ss", "dd MMM yyyy HH:mm",
      "EEE dd MMM yyyy HH:mm", "EEE, MMM dd, yyyy hh:mm:ss a", "EEE, MMM dd, yyyy hh:mm a", "EEE dd MMM yyyy HH.mm", "HH:mm MM/dd/yyyy" };

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

  public static BasicDBObject getDocument(Element e) {
    final BasicDBObject element = new BasicDBObject();
    element.put("name", e.getName());
    element.put("uid", e.getUid());
    element.put("collection", e.getCollection());

    final List<BasicDBObject> meta = new ArrayList<BasicDBObject>();
    for (MetadataRecord r : e.getMetadata()) {
      final BasicDBObject md = new BasicDBObject();
      md.put("key", r.getProperty().getId());
      md.put("value", DataHelper.getTypedValue(r.getProperty().getType(), r.getValue()));
      md.put("status", r.getStatus());
      md.put("sources", r.getSources());
      meta.add(md);
    }

    element.put("metadata", meta);

    return element;
  }

  public static BasicDBObject getDocument(Source s) {
    final BasicDBObject source = new BasicDBObject();

    source.put("_id", s.getId());
    source.put("name", s.getName());
    source.put("version", s.getVersion());

    return source;
  }

  public static BasicDBObject getDocument(Property p) {
    final BasicDBObject property = new BasicDBObject();
    property.put("_id", p.getId());
    property.put("key", p.getKey());
    property.put("type", p.getType());

    return property;
  }

  public static String getPropertyType(String key) {
    return TYPES.getProperty(key, "STRING");
  }

  public static Object getTypedValue(String t, String value) {

    if (value == null) {
      return "";
    }

    PropertyType type = PropertyType.valueOf(t);
    Object result = null;
    switch (type) {
      case BOOL:
        result = DataHelper.getBooleanValue(value);
        break;
      case INTEGER:
        result = DataHelper.getIntegerValue(value);
        break;
      case FLOAT:
        result = DataHelper.getDoubleValue(value);
        break;
      case DATE:
        result = DataHelper.getDateValue(value);
        break;
      case ARRAY:
        break;
    }

    return (result == null) ? value : result;

  }

  public static Date getDateValue(String value) {
    LOG.trace("parsing value {} as date", value);

    try {
      return new Date(Long.valueOf(value));

    } catch (NumberFormatException e) {
      LOG.trace("date is not in long representation, trying pattern matching: {}", e.getMessage());

      final SimpleDateFormat fmt = new SimpleDateFormat();

      Date result = null;
      for (String p : PATTERNS) {

        fmt.applyPattern(p);
        result = DataHelper.parseDate(fmt, value);

        if (result != null) {
          break;
        }
      }

      if (result == null) {
        LOG.debug("No pattern matching for value {}", value);
      }

      return result;
    }
  }

  private static Double getDoubleValue(String value) {
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      LOG.warn("Value {} is not an float", value);
      return null;
    }
  }

  private static Integer getIntegerValue(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      LOG.warn("Value {} is not an integer", value);
      return null;
    }
  }

  private static Boolean getBooleanValue(String value) {
    if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) {
      return new Boolean(true);
    } else if (value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")) {
      return new Boolean(false);
    } else {
      LOG.warn("Value {} is not a boolean", value);
      return null;
    }
  }

  private static Date parseDate(DateFormat fmt, String d) {
    try {
      return fmt.parse(d);
    } catch (ParseException e) {
      LOG.trace("date could not be parsed: {}", e.getMessage());
      return null;
    }
  }

  private DataHelper() {

  }
}
