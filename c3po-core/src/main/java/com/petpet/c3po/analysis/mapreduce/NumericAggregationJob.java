package com.petpet.c3po.analysis.mapreduce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Property;

public class NumericAggregationJob {
  
  private static final Logger LOG = LoggerFactory.getLogger(NumericAggregationJob.class);

  private String collection;
  private Property property;
  private PersistenceLayer persistence;
  private String filter; // the id of the filter property.
  private String value;

  public NumericAggregationJob(String c, Property p, PersistenceLayer pl) {
    this.collection = c;
    this.property = p;
    this.persistence = pl;
    this.filter = null;
    this.value = null;
  }

  public NumericAggregationJob(String c, Property p, PersistenceLayer pl, String f, String v) {
    this(c, p, pl);
    this.filter = f;
    this.value = v;
  }

  public MapReduceOutput execute() {
    String map;
    final DBCollection elements = this.persistence.getDB().getCollection(Constants.TBL_ELEMENTS);
    final BasicDBObject query = new BasicDBObject();

    if (filter == null && value == null) {
      map = Constants.AGGREGATE_MAP.replaceAll("\\{1\\}", this.property.getId());
      
    } else {
      map = Constants.FILTER_AGGREGATE_MAP.replaceAll("\\{1\\}", this.filter).replaceAll("\\{2\\}", this.value)
          .replaceAll("\\{3\\}", this.property.getId());

      query.put("metadata." + this.filter + ".value", this.value);

    }

    final MapReduceCommand cmd = new MapReduceCommand(elements, map, Constants.AGGREGATE_REDUCE, null,
        OutputType.INLINE, query);

    cmd.setFinalize(Constants.AGGREGATE_FINALIZE);

    query.put("metadata." + this.property.getId(), new BasicDBObject("$exists", true));
    query.put("collection", this.collection);

    return this.persistence.mapreduce(Constants.TBL_ELEMENTS, cmd);
  }
}
