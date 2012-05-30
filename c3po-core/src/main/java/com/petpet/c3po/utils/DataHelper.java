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

  private DataHelper() {

  }
}
