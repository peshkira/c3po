package controllers;

import helpers.Graph;
import helpers.PropertyValuesFilter;
import helpers.Statistics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.analysis.mapreduce.HistogramJob;
import com.petpet.c3po.analysis.mapreduce.MapReduceJob;
import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;

public class FilterController extends Controller {

  /**
   * Gets all selected filters and returns them to the client, so that it can
   * reconstruct the page.
   * 
   * @return
   */
  public static Result getAll() {
    Logger.debug("in method getAll(), retrieving all properties");
    List<PropertyValuesFilter> filters = new ArrayList<PropertyValuesFilter>();
    Filter filter = Application.getFilterFromSession();

    if (filter != null) {
      BasicDBObject ref = new BasicDBObject("descriminator", filter.getDescriminator());
      DBCursor cursor = Configurator.getDefaultConfigurator().getPersistence().find(Constants.TBL_FILTERS, ref);

      while (cursor.hasNext()) {
        Filter tmp = DataHelper.parseFilter(cursor.next());
        if (tmp.getProperty() != null && tmp.getValue() != null) {
          PropertyValuesFilter f = getValues(tmp.getCollection(), tmp.getProperty());
          f.setSelected(tmp.getValue());
          filters.add(f);
        }
      }
    }

    return ok(play.libs.Json.toJson(filters));

  }

  public static Result remove(String property) {
    Logger.debug("in method remove(String property), removing filter with property " + property);
    PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    Filter filter = Application.getFilterFromSession();
    BasicDBObject query = new BasicDBObject("descriminator", filter.getDescriminator());
    query.put("property", property);

    DBCursor cursor = p.find(Constants.TBL_FILTERS, query);
    if (cursor.count() == 0) {
      Logger.debug("No filter found for property: " + property);
    } else if (cursor.count() == 1) {
      Logger.debug("Removing filter for property: " + property);
      Filter tmp = DataHelper.parseFilter(cursor.next());
      p.getDB().getCollection(Constants.TBL_FILTERS).remove(tmp.getDocument());
    } else {
      Logger.error("Something went wrong, while removing filter for property: " + property);
      throw new RuntimeException("Two many filters found for property " + property);
    }

    // TODO change uid to property and value
    // find such property and value in the current filter and remove...
    return ok();
  }

  public static Result add() {
    Logger.debug("in method add(), adding new filter");
    // final List<String> names = Application.getCollectionNames();
    Filter filter = Application.getFilterFromSession();

    if (filter != null) {
      final DynamicForm form = form().bindFromRequest();
      final String f = form.get("filter");
      final String v = form.get("value");
      final String t = form.get("type");

      if (t == null || t.equals("normal")) {
        return addFromFilter(filter, f, v);
      } else if (t.equals("graph")) {
        int value = Integer.parseInt(v);
        return addFromGraph(filter, f, value);
      }
    }

    return badRequest("No filter was found in the session\n");
  }

  private static Result addFromFilter(Filter filter, String f, String v) {
    Logger.debug("in method addFromFilter(), adding new filter with property '" + f + "' and value '" + v + "'");
    PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();

    BasicDBObject ref = new BasicDBObject("descriminator", filter.getDescriminator());
    DBCursor cursor = Configurator.getDefaultConfigurator().getPersistence().find(Constants.TBL_FILTERS, ref);
    boolean existing = false;
    while (cursor.hasNext()) {
      Filter tmp = DataHelper.parseFilter(cursor.next());
      if (tmp.getProperty() != null && tmp.getProperty().equals(f)) {
        Logger.debug("Filter is already present, changing value");
        p.getDB().getCollection(Constants.TBL_FILTERS).remove(tmp.getDocument());

        tmp.setValue(v);
        p.insert(Constants.TBL_FILTERS, tmp.getDocument());
        existing = true;
        break;
      }
    }

    if (!existing) {
      Logger.info("Filtering based on new filter: " + filter + " " + v);
      Filter newFilter = new Filter(filter.getCollection(), f, v);
      newFilter.setDescriminator(filter.getDescriminator());
      p.insert(Constants.TBL_FILTERS, newFilter.getDocument());
    }

    return ok();

  }

  private static Result addFromGraph(Filter filter, String f, int value) {
    Logger.debug("in method addFromGraph(), adding new filter with property '" + f + "' and position value '" + value
        + "'");
    Logger.info("Current filter was: " + filter.getDescriminator());
    // query histogram to check the value of the filter that was selected
    final Graph graph = getGraph(filter, f);
    graph.sort();

    if (f.equals("creating_application_name")) {
      graph.cutLongTail();
    }

    final String filtervalue = graph.getKeys().get(value);

    return addFromFilter(filter, f, filtervalue);
  }

  public static Result getValues() {
    Logger.debug("in method getValues(), retrieving values for selected property");
    final DynamicForm form = form().bindFromRequest();
    final String c = form.get("collection");
    final String p = form.get("filter");

    final PropertyValuesFilter f = getValues(c, p);

    return ok(play.libs.Json.toJson(f));
  }

  private static PropertyValuesFilter getValues(String c, String p) {
    Logger.debug("get property values filter for " + c + " and property " + p);
    final Cache cache = Configurator.getDefaultConfigurator().getPersistence().getCache();
    final Property property = cache.getProperty(p);
    final MapReduceJob job = new HistogramJob(c, p);
    final MapReduceOutput output = job.execute();
    final List<BasicDBObject> jobresults = (List<BasicDBObject>) output.getCommandResult().get("results");
    final List<String> result = new ArrayList<String>();

    for (final BasicDBObject dbo : jobresults) {
      String val = dbo.getString("_id");
      if (val.endsWith(".0")) {
        val = val.substring(0, val.length() - 2);
      }
      result.add(val);
    }

    PropertyValuesFilter f = new PropertyValuesFilter();
    f.setProperty(property.getId());
    f.setType(property.getType());
    f.setValues(result);

    return f;
  }

  public static Graph getGraph(String collection, String property) {
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

  public static Graph getGraph(Filter filter, String property) {
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

  public static Statistics getCollectionStatistics(Filter filter) {
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

  public static Statistics getCollectionStatistics(String name) {
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
