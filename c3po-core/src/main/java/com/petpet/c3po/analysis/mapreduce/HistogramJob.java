package com.petpet.c3po.analysis.mapreduce;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Property;

public class HistogramJob extends MapReduceJob {

  private String property;
  
  public HistogramJob(String c, String f) {
    this.setC3poCollection(c);
    this.property = f;
    this.setFilterquery(new BasicDBObject("collection", c));
  }
  
  public HistogramJob(String c, String f, BasicDBObject query) {
    this(c, f);
    this.setFilterquery(query);
  }
  
  public MapReduceOutput execute() {
    final Property p = this.getPersistence().getCache().getProperty(property);
    final String map = Constants.HISTOGRAM_MAP.replaceAll("\\{\\}", p.getId());
    final DBCollection elements = this.getPersistence().getDB().getCollection(Constants.TBL_ELEMENTS);
    final MapReduceCommand cmd = new MapReduceCommand(elements, map, Constants.HISTOGRAM_REDUCE, this.getOutputCollection(),
        this.getType(), this.getFilterquery());

    return this.getPersistence().mapreduce(Constants.TBL_ELEMENTS, cmd);
  }
}
