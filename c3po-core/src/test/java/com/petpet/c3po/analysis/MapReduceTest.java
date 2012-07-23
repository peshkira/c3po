package com.petpet.c3po.analysis;

import junit.framework.Assert;

import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.analysis.mapreduce.HistogramJob;
import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.utils.Configurator;

public class MapReduceTest {

  @Test
  public void shouldTestAggregationMapReduce() throws Exception {
    Configurator.getDefaultConfigurator().configure();
    
    NumericAggregationJob job = new NumericAggregationJob("test1", "size");
    MapReduceOutput output = job.execute();
    
    System.out.println(output);
    Assert.assertNotNull(output);
  }
  
  @Test
  public void shouldTestHistogramMapReduce() throws Exception {
    Configurator.getDefaultConfigurator().configure();
    
    BasicDBObject query = new BasicDBObject("test1", "fao");
    query.put("metadata.mimetype.value", "application/pdf");
    
    HistogramJob job = new HistogramJob("test1", "format", query);
    MapReduceOutput output = job.execute();
    
    System.out.println(output);
    Assert.assertNotNull(output);
  }
  
  
}
