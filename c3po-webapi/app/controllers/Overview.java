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
import views.html.defaultpages.error;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.analysis.mapreduce.HistogramJob;
import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;

import common.WebAppConstants;

public class Overview extends Controller {

  public static Result index() {
    final List<String> names = Application.getCollectionNames();
    String collection = session().get(WebAppConstants.CURRENT_COLLECTION_SESSION);
    Statistics stats = null;
    GraphData data = null;
    if (collection != null) {
      stats = getCollectionStatistics(collection);
      final Graph mimes = getGraph(collection, "mimetype");
      mimes.sort();
      final Graph formats = getGraph(collection, "format");
      formats.sort();
      final Graph valid = getGraph(collection, "valid");
      valid.convertToPercentage();
      valid.sort();
      final Graph wf = getGraph(collection, "wellformed");
      wf.convertToPercentage();
      wf.sort();
      data = new GraphData(Arrays.asList(mimes, formats, valid, wf));
    }
    return ok(overview.render(names, data, stats));
  }

  public static Result filter() {
    final List<String> names = Application.getCollectionNames();
    String collection = session().get(WebAppConstants.CURRENT_COLLECTION_SESSION);
    if (collection != null) {
      // obtain data from request
      final DynamicForm form = form().bindFromRequest();
      final String filter = form.get("filter");
      final int value = Integer.parseInt(form.get("value"));

      final Graph graph = getGraph(collection, filter);
      if (filter.equals("valid") || filter.equals("wellformed")) {
        graph.convertToPercentage();
      }
      graph.sort();

      final String filtervalue = graph.getKeys().get(value);

      Logger.info("Filtering based on " + filter + " " + filtervalue);

      // get current filter from session
      String filterId = session().get(WebAppConstants.CURRENT_FILTER_SESSION);
      PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
      DBCursor c = p.find(Constants.TBL_FILTERS, new BasicDBObject("_id", filterId));
      Filter newFilter = new Filter(collection, filter, filtervalue);
      if (c.count() == 0) { // there is no filter
        
        p.insert(Constants.TBL_FILTERS, newFilter.getDocument());
      } else {
        DBObject next = c.next();
        Filter parent = DataHelper.parseFilter(next);
        parent.setMatching(newFilter);
        newFilter.setParent(parent);
        p.insert(Constants.TBL_FILTERS, parent.getDocument());
        p.insert(Constants.TBL_FILTERS, newFilter.getDocument());
      }

      session().put(WebAppConstants.CURRENT_FILTER_SESSION, newFilter.getId());

      final Graph mimes = getGraph(newFilter, "mimetype");
      mimes.sort();
      final Graph formats = getGraph(newFilter, "format");
      formats.sort();
      final Graph valid = getGraph(newFilter, "valid");
      valid.convertToPercentage();
      valid.sort();
      final Graph wf = getGraph(newFilter, "wellformed");
      wf.convertToPercentage();
      wf.sort();
      final GraphData data = new GraphData(Arrays.asList(mimes, formats, valid, wf));
      final Statistics stats = getCollectionStatistics(newFilter);

      return ok(overview.render(names, data, stats));

    }
    // if there is none
    // create a new filter based on this
    // set it as current
    // after you have the filter
    // generate the graphs again but based on the current filter
    // show them in a new view.
    return notFound("Something went wrong");
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
