package com.petpet.c3po.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.Source;

public class DBCache implements Cache {

  private Map<String, Property> propertyCache;

  private Map<String, Source> sourceCache;

  private PersistenceLayer persistence;

  public DBCache() {
    this.propertyCache = Collections.synchronizedMap(new HashMap<String, Property>());
    this.sourceCache = Collections.synchronizedMap(new HashMap<String, Source>());
  }
  
  public void setPersistence(PersistenceLayer persistence) {
    this.persistence = persistence;
  }

  /**
   * Looks in the cache for a property with the given key. If the property is
   * found in the cache it is retrieved, if it is not found in the cache, the db
   * is queried. Supposedly the property is found in the db, then it is loaded
   * into the cache and it is returned. If no property with the given key is
   * found in the db, then a new property is created, stored into the db, added
   * to the cache and then it is returned.
   * 
   * @param key
   *          the name of the property.
   * @return the property.
   */
  @Override
  public synchronized Property getProperty(String key) {
    Property property = this.propertyCache.get(key);

    if (property == null) {
      DBCursor result = this.findProperty(key);

      if (result.count() == 0) {
        property = this.createProperty(key);

      } else if (result.count() == 1) {
        property = this.extractProperty(result.next());

      } else {
        throw new RuntimeException("More than one properties found for key: " + key);
      }
    }

    return property;
  }

  @Override
  public synchronized Source getSource(String name, String version) {
    Source source = this.sourceCache.get(name + ":" + version);

    if (source == null) {
      DBCursor result = this.findSource(name, version);

      if (result.count() == 0) {
        source = this.createSource(name, version);

      } else if (result.count() == 1) {
        source = this.extractSource(result.next());

      } else {
        throw new RuntimeException("More than one sources found for name: " + name + " and version: " + version);
      }
    }

    return source;
  }
  
  @Override
  public synchronized void clear() {
    this.propertyCache.clear();
    this.sourceCache.clear();
  }

  private DBCursor findSource(String name, String version) {
    DB db = this.persistence.getDB();
    DBCollection sources = db.getCollection("sources");
    BasicDBObject query = new BasicDBObject();
    query.put("name", name);
    query.put("version", version);

    return sources.find(query);
  }

  private DBCursor findProperty(String key) {
    DB db = this.persistence.getDB();
    DBCollection properties = db.getCollection("properties");
    BasicDBObject query = new BasicDBObject();
    query.put("key", key);

    return properties.find(query);
  }

  private Property extractProperty(DBObject obj) {
    Property result = null;

    if (obj != null) {
      String id = (String) obj.get("_id");
      String key = (String) obj.get("key");
      String name = (String) obj.get("name");
      String desc = (String) obj.get("description");
      String type = (String) obj.get("type");

      result = new Property();
      result.setId(id);
      result.setKey(key);
      result.setName(name);
      result.setDescription(desc);
      result.setType(type);
    }

    return result;
  }

  private Source extractSource(DBObject obj) {
    Source result = null;

    if (obj != null) {
      String id = (String) obj.get("_id");
      String name = (String) obj.get("name");
      String version = (String) obj.get("version");

      result = new Source();
      result.setId(id);
      result.setName(name);
      result.setVersion(version);
    }

    return result;
  }

  private Source createSource(String name, String version) {
    Source s = new Source(name, version);
    this.persistence.getDB().getCollection("sources").insert(s.getDocument());
    this.sourceCache.put(name + ":" + version, s);

    return s;
  }

  private Property createProperty(String key) {
    Property p = new Property(key, key);
    this.persistence.getDB().getCollection("properties").insert(p.getDocument());
    this.propertyCache.put(key, p);

    return p;
  }

}
