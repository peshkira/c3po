package com.petpet.c3po.analysis.mapreduce;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Property;

public class HistogrammJob extends MapReduceJob {

  private String filter;
  
  public HistogrammJob(String c, String f) {
    this.setC3poCollection(c);
    this.filter = f;
  }
  
  public MapReduceOutput execute() {
    
    final Property p = this.getPersistence().getCache().getProperty(filter);
    final String map = Constants.HISTOGRAM_MAP.replaceAll("\\{\\}", p.getId());
    final DBCollection elements = this.getPersistence().getDB().getCollection(Constants.TBL_ELEMENTS);
    final BasicDBObject query = new BasicDBObject();
    final MapReduceCommand cmd = new MapReduceCommand(elements, map, Constants.HISTOGRAM_REDUCE, this.getOutputCollection(),
        this.getType(), query);

//    query.put("metadata." + p.getId(), new BasicDBObject("$exists", true));
    query.put("collection", this.getC3poCollection());
    
    return this.getPersistence().mapreduce(Constants.TBL_ELEMENTS, cmd);
  }
}
