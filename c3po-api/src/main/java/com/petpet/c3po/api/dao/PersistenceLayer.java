package com.petpet.c3po.api.dao;

import java.util.Map;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.GroupCommand;


public interface PersistenceLayer {
  
  Cache getCache();

  DB getDB();
  
  DB connect(Map<String, String> config);
  
  void close();
  
  DBCursor findAll(String collection);
  
  DBCursor find(String collection, DBObject ref);
  
  DBCursor find(String collection, DBObject ref, DBObject keys);
  
  void insert(String collection, DBObject data);
  
  long count(String collection);
  
  DBObject group(String collection, GroupCommand cmd);
  
}
