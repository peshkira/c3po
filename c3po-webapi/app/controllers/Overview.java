package controllers;

import helpers.Filter;
import helpers.Graph;
import helpers.GraphData;
import helpers.Statistics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.overview;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.analysis.mapreduce.HistogrammJob;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Configurator;

public class Overview extends Controller {

  public static Result index() {
    final List<String> names = Application.getCollectionNames();
    String collection = session().get("current.collection");
    Statistics stats = null;
    GraphData data = null;
    if (collection != null) {
      stats = getCollectionStatistics(collection);
      final Graph mimes = getGraph(collection, "mimetype");
      final Graph formats = getGraph(collection, "format");
      final Graph valid = getGraph(collection, "valid");
      final Graph wf = getGraph(collection, "wellformed");
      data = new GraphData(Arrays.asList(mimes, formats, valid, wf));
    }
    return ok(overview.render(names, data, stats));
  }

  public static Result show() {
    // final Form<Filter> form = form(Filter.class).bindFromRequest();
    // final Filter data = form.get();
    // Logger.info("Called Show with name " + data.getCollection() +
    // " and filter " + data.getFilter());
    // List<String> collections = Application.getCollectionNames();
    //
    // final Graph mimes = getGraph(data, "mimetype");
    // final Graph formats = getGraph(data, "format");
    // final Graph fv = getGraph(data, "format_version");
    // final Graph valid = getGraph(data, "valid");
    // final Graph wellformed = getGraph(data, "wellformed");
    //
    //
    // final GraphData graphs = new GraphData(Arrays.asList(mimes, formats, fv,
    // valid, wellformed));
    //
    // if (data.getFilter().equals("mimetype")) {
    // data.setValues(mimes.getKeys());
    // } else if (data.getFilter().equals("format")) {
    // data.setValues(formats.getKeys());
    // }
    // form.fill(data);
    // Statistics stats = getCollectionStatistics(data.getCollection(),
    // data.getFilter(), data.getValue());
    // return ok(overview.render(collections, form, graphs, stats));
    return ok();
  }

  private static Graph getGraph(String collection, String property) {
    final PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    final List<String> keys = new ArrayList<String>();
    final List<String> values = new ArrayList<String>();
    final Graph result = new Graph(property, keys, values);

    DBCollection dbc = p.getDB().getCollection("histogram_" + collection + "_" + property);

    if (dbc == null) {
      final HistogrammJob job = new HistogrammJob(collection, property);
      final MapReduceOutput output = job.execute();
      final List<BasicDBObject> jobresults = (List<BasicDBObject>) output.getCommandResult().get("results");

      for (final BasicDBObject dbo : jobresults) {
        keys.add((dbo.getString("_id")));
        values.add(dbo.getString("value"));
      }
    } else {
      DBCursor cursor = dbc.find();
      while (cursor.hasNext()) {
        BasicDBObject dbo = (BasicDBObject) cursor.next();
        keys.add(dbo.getString("_id"));
        values.add(dbo.getString("value"));
      }
    }
    return result;
  }

  private static Graph getGraph(Filter data, String property) {
    final List<String> keys = new ArrayList<String>();
    final List<String> values = new ArrayList<String>();
    final Graph result = new Graph(property, keys, values);

    if (data != null && data.getFilter() != null) {
      final PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
      HistogrammJob job = null;
      job = new HistogrammJob(data.getCollection(), property);

      final MapReduceOutput output = job.execute();
      final List<BasicDBObject> jobresults = (List<BasicDBObject>) output.getCommandResult().get("results");

      for (final BasicDBObject dbo : jobresults) {
        keys.add((dbo.getString("_id")));
        values.add(dbo.getString("value"));
      }
    }
    return result;
  }

  private static Statistics getCollectionStatistics(String name) {
    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    BasicDBObject aggregation = null;

    DBCollection collection = pl.getDB().getCollection("statistics_" + name);
    if (collection != null) {
      aggregation = (BasicDBObject) collection.findOne().get("value");
    }

    if (aggregation == null) {

      Property size = pl.getCache().getProperty("size");
      NumericAggregationJob job = new NumericAggregationJob(name, size);

      job.setType(OutputType.REPLACE);
      job.setOutputCollection("statistics_" + name);
      MapReduceOutput output = job.execute();
      final List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");

      if (results.isEmpty()) {
        return null;
      } else {
        aggregation = (BasicDBObject) results.get(0).get("value");
      }
    }

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
