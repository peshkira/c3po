package controllers;

import helpers.Filter;
import helpers.Statistics;

import java.util.Collections;
import java.util.List;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import com.mongodb.BasicDBObject;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Configurator;

public class Application extends Controller {

  public static Result index() {
    List<String> collections = getCollectionNames();

    return ok(index.render(null, collections, null));
  }

  public static Result show(String name, String filter) {
    Logger.info("Called Show with name " + name + " and filter "+ filter);
    List<String> collections = getCollectionNames();
    
    Statistics stats = getCollectionStatistics(name, filter, "application/pdf");
    return ok(index.render(name, collections, stats));
  }

  private static List<String> getCollectionNames() {
    PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
    List<String> collections = (List<String>) persistence.distinct(Constants.TBL_ELEMENTS, "collection");
    Collections.sort(collections);
    return collections;

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