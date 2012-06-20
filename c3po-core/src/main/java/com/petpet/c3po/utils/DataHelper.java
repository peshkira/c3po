package com.petpet.c3po.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.MetadataRecord;
import com.petpet.c3po.datamodel.Property;

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
    
    Element e  = new Element(coll, uid, name);
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
      
      List<String> values = (List<String>) prop.get("values");
      if (values != null) {
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


  private DataHelper() {

  }
}
