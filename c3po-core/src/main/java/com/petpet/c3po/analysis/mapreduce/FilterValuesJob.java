package com.petpet.c3po.analysis.mapreduce;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Property;

public class FilterValuesJob {

  private String collection;
  private String filter;
  private PersistenceLayer persistence;
  
  public FilterValuesJob(String c, String f, PersistenceLayer pl) {
    this.collection = c;
    this.filter = f;
    this.persistence = pl;
  }
  
  public MapReduceOutput execute() {
    
    final Property p = this.persistence.getCache().getProperty(filter);
    final String map = Constants.HISTOGRAM_MAP.replaceAll("\\{\\}", p.getId());
    final DBCollection elements = this.persistence.getDB().getCollection(Constants.TBL_ELEMENTS);
    final BasicDBObject query = new BasicDBObject();
    final MapReduceCommand cmd = new MapReduceCommand(elements, map, Constants.HISTOGRAM_REDUCE, null,
        OutputType.INLINE, query);

    query.put("metadata." + p.getId(), new BasicDBObject("$exists", true));
    query.put("collection", collection);
    
    return this.persistence.mapreduce(Constants.TBL_ELEMENTS, cmd);
  }
}
