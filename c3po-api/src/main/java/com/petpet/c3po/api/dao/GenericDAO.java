package com.petpet.c3po.api.dao;

import java.util.List;

public interface GenericDAO<TYPE> {

  TYPE persist(TYPE type);
  
  TYPE update(TYPE item);

  void delete(TYPE item);
  
  TYPE findById(Long id);
  
  List<TYPE> findAll();
}
