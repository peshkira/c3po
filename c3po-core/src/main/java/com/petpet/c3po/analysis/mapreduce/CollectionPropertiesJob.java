package com.petpet.c3po.analysis.mapreduce;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.api.model.ActionLog;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.utils.ActionLogHelper;

public class CollectionPropertiesJob extends MapReduceJob {

  public CollectionPropertiesJob() {

  }

  public CollectionPropertiesJob(String collection) {
    this.setC3poCollection(collection);
    this.setFilterquery(new BasicDBObject("collection", collection));
    this.setOutputCollection(collection + "_properties");
    this.setType(OutputType.REPLACE);
  }

  @Override
  public MapReduceOutput execute() {
    final ActionLogHelper alHelper = new ActionLogHelper(this.getPersistence());
    return runCommand(alHelper);
  }

  @Override
  public JobResult run() {
    final ActionLogHelper alHelper = new ActionLogHelper(this.getPersistence());

    JobResult output = null;

    ActionLog lastAction = alHelper.getLastAction(this.getC3poCollection());

    if (lastAction == null || lastAction.getAction().equals(ActionLog.UPDATED_ACTION)) {
      MapReduceOutput mOutput = this.runCommand(alHelper);

      String outputColl = (String) mOutput.getCommandResult().get("result");
      if (outputColl != null) {
        output = getFromDB(alHelper);
      } else {
        output = new JobResult(mOutput);
      }

    } else {
      output = getFromDB(alHelper);
    }

    return output;
  }

  private JobResult getFromDB(ActionLogHelper alHelper) {
    JobResult output;

    DBCursor cursor = this.getPersistence().findAll(this.getC3poCollection() + "_properties");
    if (cursor.count() > 0) {
      
      List<BasicDBObject> results = new ArrayList<BasicDBObject>();
      while (cursor.hasNext()) {
        results.add((BasicDBObject) cursor.next());
      }
      
      output = new JobResult();
      output.setResults(results);

    } else {
      MapReduceOutput mOutput = this.runCommand(alHelper);
      output = new JobResult(mOutput);
    }

    return output;
  }

  private MapReduceOutput runCommand(final ActionLogHelper alHelper) {
    final DBCollection elements = this.getPersistence().getDB().getCollection(Constants.TBL_ELEMENTS);
    final MapReduceCommand cmd = new MapReduceCommand(elements, Constants.PROPERTIES_IN_COLLECTION_MAP,
        Constants.PROPERTIES_IN_COLLECTION_REDUCE, this.getOutputCollection(), this.getType(), this.getFilterquery());
    final MapReduceOutput output = this.getPersistence().mapreduce(Constants.TBL_ELEMENTS, cmd);
    alHelper.recordAction(new ActionLog(this.getC3poCollection(), ActionLog.ANALYSIS_ACTION));

    return output;
  }

}
