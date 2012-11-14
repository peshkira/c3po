package com.petpet.c3po.analysis.mapreduce;

import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.MapReduceOutput;

public class JobResult {

  private List<BasicDBObject> results;

  public JobResult() {

  }

  public JobResult(MapReduceOutput output) {
    this.setResults((List<BasicDBObject>) output.getCommandResult().get("results"));
  }

  public List<BasicDBObject> getResults() {
    return results;
  }

  public void setResults(List<BasicDBObject> results) {
    this.results = results;
  }
}
