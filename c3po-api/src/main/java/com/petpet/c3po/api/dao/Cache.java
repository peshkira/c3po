package com.petpet.c3po.api.dao;

import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.Source;

public interface Cache {

  Property getProperty(String key);
  
  Source getSource(String name, String version);
  
  void clear();
}
