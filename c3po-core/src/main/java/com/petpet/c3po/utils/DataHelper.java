package com.petpet.c3po.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.MetadataRecord;

public final class DataHelper {

  private static final Logger LOG = LoggerFactory.getLogger(DataHelperTest.class);

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
  
  public static Element parseElement(final DBObject obj, final PersistenceLayer pl) {
    String coll = (String) obj.get("collection");
    String uid = (String) obj.get("uid");
    String name = (String) obj.get("name");
    
    Element e  = new Element(coll, uid, name);
    e.setId(obj.get("_id").toString());
    
    DBObject meta = (BasicDBObject) obj.get("metadata");
    for (String key : meta.keySet()) {
      System.out.println(key);
      MetadataRecord rec = new MetadataRecord();
      DBObject prop = (DBObject) meta.get(key);
//      rec.setProperty(pl.getCache().getProperty(key))
    }
    
    return e;
  }


  private DataHelper() {

  }
}
