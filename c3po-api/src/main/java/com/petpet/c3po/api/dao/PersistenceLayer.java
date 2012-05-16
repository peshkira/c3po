package com.petpet.c3po.api.dao;

import java.util.Map;

import com.mongodb.DB;


public interface PersistenceLayer {
  
  DB getDB();
  
  Cache getCache();
  
  DB connect(Map<String, String> config);
  
  void close();
}
