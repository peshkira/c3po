package com.petpet.c3po.api.dao;

import java.util.Map;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * The persistence layer interface offers some common methods for interacting
 * with the mongo database.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface PersistenceLayer {

  DB connect(Map<String, String> config);
  
  boolean isConnected();

  void close();
  
  DB getDB();

  Cache getCache();

  void clearCache();

  DBCursor findAll(String collection);

  DBCursor find(String collection, DBObject ref);

  DBCursor find(String collection, DBObject ref, DBObject keys);

  void insert(String collection, DBObject data);

  long count(String collection);

}
