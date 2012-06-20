package controllers;

import helpers.Filter;
import helpers.Statistics;

import java.text.DecimalFormat;
import java.util.List;

import play.Logger;
import play.data.Form;
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
    // overview.render(null, names, null)
    overview.render(names, form(Filter.class), null, null));
  }

  public static Result show() {
    final Form<Filter> form = form(Filter.class).bindFromRequest();
    final Filter data = form.get();
    Logger.info("Called Show with name " + data.getCollection() + " and filter " + data.getFilter());
    List<String> collections = Application.getCollectionNames();

    Statistics stats = getCollectionStatistics(data.getCollection(), data.getFilter(), "application/pdf"); // TODO
                                                                                                           // change
                                                                                                           // this
                                                                                                           // to
                                                                                                           // be
                                                                                                           // dynamic...
    return ok(overview.render(collections, form, null, stats));
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
    System.out.println(aggregation);
    final DecimalFormat df = new DecimalFormat("#.##");
    Statistics stats = new Statistics();
    stats.setCount(aggregation.getInt("count") + " objects");
    stats.setSize(df.format(aggregation.getLong("sum") / 1024D / 1024) + " MB");
    stats.setAvg(df.format(aggregation.getDouble("avg") / 1024 / 1024) + " MB");
    stats.setMin(aggregation.getLong("min") + " B");
    stats.setMax(df.format(aggregation.getLong("max") / 1024D / 1024) + " MB");
    stats.setSd(df.format(aggregation.getDouble("stddev") / 1024 / 1024) + " MB");
    stats.setVar(df.format(aggregation.getDouble("variance") / 1024 / 1024 / 1024 / 1024) + " MB");
    // because of sd^2
    return stats;
  }
}
