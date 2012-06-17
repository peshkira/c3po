package controllers;

import helpers.Statistics;

import java.util.List;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.overview;

import com.mongodb.BasicDBObject;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Configurator;


public class Overview extends Controller {
  
  public static Result index() {
    final List<String> names = Application.getCollectionNames();
    return ok(
        overview.render(null, names, null)
        );
  }
  

  public static Result show(String name, String filter) {
    Logger.info("Called Show with name " + name + " and filter "+ filter);
    List<String> collections = Application.getCollectionNames();
    
    Statistics stats = getCollectionStatistics(name, filter, "application/pdf"); //TODO change this to be dynamic...
    return ok(
        overview.render(name, collections, stats)
        );
  }

  private static Statistics getCollectionStatistics(String name, String filter, String value) {
    PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    Property size = pl.getCache().getProperty("size");
    
    NumericAggregationJob job;
    if (filter.equals("none") || value == null) {
      job = new NumericAggregationJob(name, size, pl);
    } else {
      Property f = pl.getCache().getProperty(filter);
      job = new NumericAggregationJob(name, size, pl, f.getId(), value);
    }
    
    MapReduceOutput output = job.execute();
    final List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
    
    if (results.isEmpty()) {
      return null;
    }
    
    final BasicDBObject aggregation = (BasicDBObject) results.get(0).get("value");

    Statistics stats = new Statistics();
    stats.setCount(aggregation.getInt("count"));
    stats.setSize(aggregation.getLong("sum"));
    stats.setAvg(aggregation.getDouble("avg"));
    stats.setMin(aggregation.getLong("min"));
    stats.setMax(aggregation.getLong("max"));
    stats.setSd(aggregation.getDouble("stddev"));
    stats.setVar(aggregation.getDouble("variance"));
    return stats;
  }
}
