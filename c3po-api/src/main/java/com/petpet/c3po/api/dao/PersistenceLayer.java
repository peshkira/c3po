package com.petpet.c3po.api.dao;

import java.util.List;
import java.util.Map;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;

/**
 * The persistence layer interface offers some common methods for interacting
 * with the mongo database.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface PersistenceLayer {

  DB connect(Map<Object, Object> config);
  
  boolean isConnected();

  void close();
  
  DB getDB();

  Cache getCache();

  void clearCache();

  DBCursor findAll(String collection);

  DBCursor find(String collection, DBObject ref);

  DBCursor find(String collection, DBObject ref, DBObject keys);
  
  List distinct(String collection, String key);
  
  List distinct(String collection, String key, DBObject query);

  void insert(String collection, DBObject data);

  long count(String collection);
  
  long count(String collection, DBObject query);
  
  MapReduceOutput mapreduce(String collection, MapReduceCommand cmd);

}
