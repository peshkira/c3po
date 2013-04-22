package com.petpet.c3po.dao;

import java.util.Iterator;

import org.junit.Before;

import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;

public class MongoPersistenceLayerTest {

  MongoPersistenceLayer pLayer;
  
  @Before
  public void setup() {
    pLayer = new MongoPersistenceLayer();
  }
  
  public void shouldTestFind() {
    Iterator<Element> iter = pLayer.find(Element.class, null);
    
  }
}
