package com.petpet.c3po.dao;

import junit.framework.Assert;

import org.junit.Test;

import com.mongodb.MapReduceOutput;
import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Configurator;

public class MapReduceTest {

  @Test
  public void shouldTestAggregationMapReduce() throws Exception {
    Configurator.getDefaultConfigurator().configure();
    PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    Property size = pl.getCache().getProperty("size");
    
    NumericAggregationJob job = new NumericAggregationJob("test1", size);
    MapReduceOutput output = job.execute();
    
    System.out.println(output);
    Assert.assertNotNull(output);
  }
}
