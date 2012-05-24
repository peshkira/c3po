package com.petpet.c3po.dao;

import java.net.UnknownHostException;
import java.util.Map;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.utils.DBCache;

public class LocalPersistenceLayer implements PersistenceLayer {

  private Mongo mongo;

  private DB db;

  private Cache dBCache;

  private boolean connected;

  public LocalPersistenceLayer(Map<String, String> config) {
    this.connected = false;
    this.dBCache = new DBCache(this);
    this.connect(config);
  }

  @Override
  public DB getDB() {
    return this.db;
  }

  @Override
  public DB connect(Map<String, String> config) {
    this.close();

    try {
      this.mongo = new Mongo(config.get("host"), Integer.parseInt(config.get("port")));
      this.db = this.mongo.getDB(config.get("db.name"));
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
  public boolean isConnected() {
    return this.connected;
  }

  @Override
  public void close() {
    if (this.isConnected() && this.mongo != null) {
      this.mongo.close();
      this.db = null;
      this.connected = false;
    }
  }

  @Override
  public Cache getCache() {
    return this.dBCache;
  }
  
  @Override
  public void clearCache() {
    this.dBCache.clear();

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
  public void insert(String collection, DBObject data) {
    this.db.getCollection(collection).insert(data);
  }

  @Override
  public long count(String collection) {
    return this.db.getCollection(collection).getCount();
  }

}
