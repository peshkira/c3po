package com.petpet.c3po.dao.mongo.mapreduce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.common.Constants;

@Deprecated
public class NumericAggregationJob extends MapReduceJob {

  private static final Logger LOG = LoggerFactory.getLogger(NumericAggregationJob.class);

  private String property;

  public NumericAggregationJob(String c, String p) {
    this.setC3poCollection(c);
    this.property = p;
    this.setFilterquery(new BasicDBObject("collection", c));
  }

  // public NumericAggregationJob(String c, String p, String f, String v) {
  // this(c, p);
  // this.filter = f;
  // this.value = v;
  // }

  public MapReduceOutput execute() {
    String map;
    final DBCollection elements = this.getPersistence().getDB().getCollection(Constants.TBL_ELEMENTS);
    map = Constants.AGGREGATE_MAP.replaceAll("\\{1\\}", this.property);

    final MapReduceCommand cmd = new MapReduceCommand(elements, map, Constants.AGGREGATE_REDUCE,
        this.getOutputCollection(), this.getType(), this.getFilterquery());

    cmd.setFinalize(Constants.AGGREGATE_FINALIZE);

     this.getFilterquery().put("metadata." + this.property, new BasicDBObject("$exists", true));
    // query.put("collection", this.getC3poCollection());
    return this.getPersistence().mapreduce(Constants.TBL_ELEMENTS, cmd);
  }

  @Override
  public JobResult run() {
    return new JobResult(this.execute());
  }
}
