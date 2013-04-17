package com.petpet.c3po.dao.mongo;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Model;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

public class MongoPersistenceLayer implements PersistenceLayer {

  private Mongo mongo;

  private DB db;

  private Cache dBCache;

  private boolean connected;

  public MongoPersistenceLayer() {

  }

  @Override
  public void establishConnection(Map<String, String> config) throws C3POPersistenceException {
//    this.close();
//
//    try {
//      this.mongo = new Mongo((String) config.get(Constants.CNF_DB_HOST), Integer.parseInt((String) config
//          .get(Constants.CBF_DB_PORT)));
//      this.db = this.mongo.getDB((String) config.get(Constants.CNF_DB_NAME));
//
//      this.db.getCollection(Constants.TBL_ELEMENTS).ensureIndex(new BasicDBObject("uid", 1),
//          new BasicDBObject("unique", true));
//      this.db.getCollection(Constants.TBL_PROEPRTIES).ensureIndex("_id");
//      this.db.getCollection(Constants.TBL_PROEPRTIES).ensureIndex("key");
//
//      this.connected = true;
//
//    } catch (NumberFormatException e) {
//      e.printStackTrace();
//    } catch (UnknownHostException e) {
//      e.printStackTrace();
//    } catch (MongoException e) {
//      e.printStackTrace();
//    }
//
//     return this.db;

  }

  @Override
  public void close() throws C3POPersistenceException {
    // if (this.isConnected() && this.mongo != null) {
    // this.mongo.close();
    // this.db = null;
    // this.connected = false;
    // }
  }

  @Override
  public boolean isConnected() {
    return this.connected;
  }

  @Override
  public Cache getCache() {
    return this.dBCache;
  }

  @Override
  public void setCache(Cache c) {
    this.dBCache = c;

  }

  @Override
  public void clearCache() {
    this.dBCache.clear();

  }

  @Override
  public <T extends Model> Iterator<T> find(Class<T> clazz, Filter filter) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Model> void insert(T object) {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends Model> void update(T object) {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends Model> void remove(T object) {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends Model> void remove(Class<T> clazz, Filter filter) {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends Model> long count(Class<T> clazz, Filter filter) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public <T extends Model> List<String> distinct(Class<T> clazz, Property p, Filter filter) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Model> Map<String, Integer> getValueHistogramFor(Class<T> clazz, Property p, Filter filter)
      throws UnsupportedOperationException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends Model> NumericStatistics getNumericStatistics(Class<T> clazz, Property p, Filter filter)
      throws UnsupportedOperationException, IllegalArgumentException {
    // TODO Auto-generated method stub
    return null;
  }
  
  
  

  // -- TO BE REMOVED IN VERSION 0.4.0

  @Override
  public DB connect(Map<Object, Object> config) {
    try {
      
      this.close();
      
    } catch (C3POPersistenceException e1) {
      e1.printStackTrace();
    }

    try {
      this.mongo = new Mongo((String) config.get(Constants.CNF_DB_HOST), Integer.parseInt((String) config
          .get(Constants.CBF_DB_PORT)));
      this.db = this.mongo.getDB((String) config.get(Constants.CNF_DB_NAME));

      this.db.getCollection(Constants.TBL_ELEMENTS).ensureIndex(new BasicDBObject("uid", 1),
          new BasicDBObject("unique", true));
      this.db.getCollection(Constants.TBL_PROEPRTIES).ensureIndex("_id");
      this.db.getCollection(Constants.TBL_PROEPRTIES).ensureIndex("key");

      this.connected = true;

    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (MongoException e) {
      e.printStackTrace();
    }

    return this.db;
  }

  @Override
  public DB getDB() {
    return this.db;
  }

  @Override
  public DBCursor findAll(String collection) {
    return this.db.getCollection(collection).find();
  }

  @Override
  public DBCursor find(String collection, DBObject ref) {
    return this.db.getCollection(collection).find(ref);
  }

  @Override
  public DBCursor find(String collection, DBObject ref, DBObject keys) {
    return this.db.getCollection(collection).find(ref, keys);
  }

  @Override
  public List distinct(String collection, String key) {
    return this.db.getCollection(collection).distinct(key);
  }

  @Override
  public List distinct(String collection, String key, DBObject query) {
    return this.db.getCollection(collection).distinct(key, query);
  }

  @Override
  public void insert(String collection, DBObject data) {
    this.db.getCollection(collection).insert(data);
  }

  @Override
  public long count(String collection) {
    return this.db.getCollection(collection).getCount();
  }

  @Override
  public long count(String collection, DBObject query) {
    return this.db.getCollection(collection).count(query);
  }

  @Override
  public MapReduceOutput mapreduce(String collection, MapReduceCommand cmd) {
    return this.db.getCollection(collection).mapReduce(cmd);
  }

}
