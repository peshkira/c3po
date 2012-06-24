package controllers;

import helpers.Graph;
import helpers.GraphData;
import helpers.Statistics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.overview;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.analysis.mapreduce.HistogramJob;
import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;
import common.WebAppConstants;

public class Overview extends Controller {

  public static Result index() {
    final List<String> names = Application.getCollectionNames();
    Filter filter = Application.getFilterFromSession();

    Statistics stats = null;
    GraphData data = null;
    if (filter != null) {
      final Graph mimes;
      final Graph formats;
      final Graph valid;
      final Graph wf;
      Logger.info("filter is not null");
      if (filter.getParent() == null) {
        Logger.info("filter has no parent, using cached statistics");
        // used cached results
        stats = getCollectionStatistics(filter.getCollection());
        mimes = getGraph(filter.getCollection(), "mimetype");
        formats = getGraph(filter.getCollection(), "format");
        valid = getGraph(filter.getCollection(), "valid");
        wf = getGraph(filter.getCollection(), "wellformed");

      } else {
        // calculate new results
        Logger.info("filter has parent, calculating statisticts");
        stats = getCollectionStatistics(filter);
        mimes = getGraph(filter, "mimetype");
        formats = getGraph(filter, "format");
        valid = getGraph(filter, "valid");
        wf = getGraph(filter, "wellformed");
      }
      
      mimes.sort();
      formats.sort();
      valid.convertToPercentage();
      valid.sort();
      wf.convertToPercentage();
      wf.sort();

      data = new GraphData(Arrays.asList(mimes, formats, valid, wf));
    }
    return ok(overview.render(names, data, stats));
  }

  public static Result filter() {
    // final List<String> names = Application.getCollectionNames();
    Filter filter = Application.getFilterFromSession();
    
    if (filter != null) {
      Logger.info("Current filter was: " + filter.getId());
      // obtain data from request
      final DynamicForm form = form().bindFromRequest();
      final String f = form.get("filter");
      final int value = Integer.parseInt(form.get("value"));

      // query histogram to check the value of the filter that was selected
      final Graph graph = getGraph(filter, f);
      if (f.equals("valid") || f.equals("wellformed")) {
        graph.convertToPercentage();
      }
      graph.sort();

      final String filtervalue = graph.getKeys().get(value);

      if (f.equals(filter.getProperty()) && filtervalue.equals(filter.getValue())) {
        Logger.debug("Filter matches last filter, skipping");
        return index(); // return filtering based on current filter.
      }

      Logger.info("Filtering based on new filter: " + filter + " " + filtervalue);
      // get current filter from session
      PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();

      Filter newFilter = new Filter(filter.getCollection(), f, filtervalue);
      newFilter.setParent(filter);
      p.insert(Constants.TBL_FILTERS, newFilter.getDocument());

      session().put(WebAppConstants.CURRENT_FILTER_SESSION, newFilter.getId());

      return index();

    }

    return badRequest("No filter was found in the session\n");
  }

  private static Graph getGraph(String collection, String property) {
    final PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    final List<String> keys = new ArrayList<String>();
    final List<String> values = new ArrayList<String>();
    final Graph result = new Graph(property, keys, values);

    DBCollection dbc = p.getDB().getCollection("histogram_" + collection + "_" + property);

    if (dbc.find().count() == 0) {
      final HistogramJob job = new HistogramJob(collection, property);
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

  private static Graph getGraph(Filter filter, String property) {
    final List<String> keys = new ArrayList<String>();
    final List<String> values = new ArrayList<String>();
    final Graph result = new Graph(property, keys, values);

    BasicDBObject query = Application.getFilterQuery(filter);

    HistogramJob job = new HistogramJob(filter.getCollection(), property, query);

    final MapReduceOutput output = job.execute();
    final List<BasicDBObject> jobresults = (List<BasicDBObject>) output.getCommandResult().get("results");
    for (final BasicDBObject dbo : jobresults) {
      keys.add((dbo.getString("_id")));
      values.add(dbo.getString("value"));
    }
    return result;
  }

  private static Statistics getCollectionStatistics(Filter filter) {
    final NumericAggregationJob job = new NumericAggregationJob(filter.getCollection(), "size");
    final BasicDBObject query = Application.getFilterQuery(filter);
    job.setFilterquery(query);

    final MapReduceOutput output = job.execute();
    final List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
    BasicDBObject aggregation = null;

    if (!results.isEmpty()) {
      aggregation = (BasicDBObject) results.get(0).get("value");
    }

    return getStatisticsFromResult(aggregation);
  }

  private static Statistics getCollectionStatistics(String name) {
    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    BasicDBObject aggregation = null;

    DBCollection collection = pl.getDB().getCollection("statistics_" + name);
    if (collection.find().count() != 0) {
      aggregation = (BasicDBObject) collection.findOne().get("value");
    }

    if (aggregation == null) {
      final NumericAggregationJob job = new NumericAggregationJob(name, "size");
      job.setType(OutputType.REPLACE);
      job.setOutputCollection("statistics_" + name);
      final MapReduceOutput output = job.execute();

      if (output != null) {
        aggregation = (BasicDBObject) collection.findOne().get("value");
      }
    }

    return getStatisticsFromResult(aggregation);
  }

  public static Statistics getStatisticsFromResult(BasicDBObject aggregation) {
    if (aggregation == null)
      return null;

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
