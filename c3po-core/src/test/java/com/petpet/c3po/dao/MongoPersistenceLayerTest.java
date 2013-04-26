package com.petpet.c3po.dao;

import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.utils.Configurator;

public class MongoPersistenceLayerTest {

  MongoPersistenceLayer pLayer;
  
  @Before
  public void setup() {
    pLayer = new MongoPersistenceLayer();
  }
  
  public void shouldTestFind() {
    Iterator<Element> iter = pLayer.find(Element.class, null);
    
  }
  
  //@Test
  public void shouldTestNumericAggregation() throws Exception {
    Configurator.getDefaultConfigurator().configure();
    PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
    Property property = persistence.getCache().getProperty("pagecount");
    NumericStatistics numericStatistics = persistence.getNumericStatistics(property, new Filter(new FilterCondition("collection", "govdocs")));
    System.out.println(numericStatistics.getCount());
  }
  
  //@Test
  public void shouldTestHistogramGeneration() throws Exception {
    Configurator.getDefaultConfigurator().configure();
    PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
    Property mimetype = persistence.getCache().getProperty("created");
    long start = System.currentTimeMillis();
    Map<String, Long> mimetypeHistogram = persistence.getValueHistogramFor(mimetype, null);
    long end = System.currentTimeMillis();
    
    for (String val : mimetypeHistogram.keySet()) {
      System.out.println(val + ": " + mimetypeHistogram.get(val));
    }
    
    System.out.println("Time: " + (end - start));
  }
}
