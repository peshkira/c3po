package com.petpet.c3po.api.dao;

import com.mongodb.DB;


public interface PersistenceLayer {
  
//  Object handleCreate(Class<?> clazz, Object o);
//  
//  Object handleUpdate(Class<?> clazz, Object o);
//  
//  void handleDelete(Class<?> clazz, Object o);
//  
//  Object handleFindById(Class<?> clazz, Object id);
//  
//  List<?> handleFindAll(Class<?> clazz);
//
//  EntityManager getEntityManager();
//  
//  void recover();
  
  DB getDB();
}
