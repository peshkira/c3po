/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.MapReduceOutput;

import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.dao.mongo.MongoFilterSerializer;
import com.petpet.c3po.utils.Configurator;

import common.WebAppConstants;
import helpers.Distribution;
import helpers.Graph;
import helpers.PropertyValuesFilter;
import helpers.SessionFilters;
import helpers.Statistics;
import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;

public class FilterController extends Controller {

	public static Result add() {
		Logger.debug("Received an add call in filter");
		// final List<String> names = Application.getCollectionNames();
		Filter filter = FilterController.getFilterFromSessionNotSafe();

		if (filter != null) {
			final DynamicForm form = form().bindFromRequest();
			final String f = form.get("filter");
			final String v = form.get("value");
			final String t = form.get("type");
			final String a = form.get("alg");
			final String w = form.get("width");

			if (t == null || t.equals("normal")) {
				addFromFilter(filter, f, v);
			} else if (t.equals("graph")) {
				int value = Integer.parseInt(v);
				addFromGraph(filter, f, value, a, w);
			}
			return ok();
		}

		return badRequest("No filter was found in the session\n");
	}

	private static void addFromFilter(Filter filter, String f, String v) {
		Logger.debug("Adding new filter with property '" + f + "' and value '" + v + "'");
		filter.addFilterCondition(new FilterCondition(f,v));
	}

	private static void addFromGraph(Filter filter, String prop, int value, String alg, String width) {
		Logger.debug("Adding new filter with property '" + prop + "' and position value '" + value
				+ "'");
		// query histogram to check the value of the filter that was selected
		Graph graph = null;
		Property property= Configurator.getDefaultConfigurator().getPersistence().getCache().getProperty( prop );
		//if (property.getType().equals(PropertyType.INTEGER)) {
	//		graph = getNumericGraph(filter, prop, alg, width);
	//	} else {
			graph = getGraph(filter, prop);
	//	}

		final String filtervalue = graph.getKeys().get(value);
		addFromFilter(filter, prop, filtervalue);
	}

	/*private static void calculateHistogramResults(MapReduceOutput output, List<String> keys, List<String> values) {
		final List<BasicDBObject> jobresults = (List<BasicDBObject>) output.getCommandResult().get("results");
		for (final BasicDBObject dbo : jobresults) {
			parseHistogram(dbo, keys, values);
		}
	}

	private static void calculateNumericHistogramResults(MapReduceOutput output, List<String> keys, List<String> values,
			long width) {
		List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
		for (BasicDBObject obj : results) {

			String id = obj.getString("_id");

			if (!id.equals("Unknown") && !id.equals("Conflicted")) {
				long low = (int) Double.parseDouble(id) * width;
				long high = low + width - 1;
				keys.add(low + " - " + high);
			} 
			values.add(obj.getString("value"));
		}
	}*/

	/**
	 * Gets all selected filters and returns them to the client, so that it can
	 * reconstruct the page.
	 * 
	 * @return
	 */
	public static Result getAll() {
		Logger.debug("Received a getAll call in filter");
		//PersistenceLayer pl=Configurator.getDefaultConfigurator().getPersistence();
		List<PropertyValuesFilter> filters = new ArrayList<PropertyValuesFilter>();
		//Filter filter = FilterController.getFilterFromSession();
		//if (filter != null) {
		List<String> properties=Application.getPropertyNames();
		for (String prop: properties){
			Distribution d=Application.getDistribution(prop);
			PropertyValuesFilter f=new PropertyValuesFilter();
			f.setProperty(d.getProperty());
			f.setValues(d.getPropertyValues());
			filters.add(f);
		}
		//}
		return ok(play.libs.Json.toJson(filters));

	}

	public static Statistics getCollectionStatistics(Filter filter) {
		Distribution sizeDistribution = Application.getDistribution("size",filter);
		final PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		Property size =  persistence.getCache().getProperty( "size" );  
		NumericStatistics statistics =  persistence.getNumericStatistics( size, filter );
		/* final NumericAggregationJob job = new NumericAggregationJob(filter.getCollection(), "size");
    final BasicDBObject query = Application.getFilterQuery(filter);
    job.setFilterquery(query);

    final MapReduceOutput output = job.execute();
    final List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
    BasicDBObject aggregation = null;

    if (!results.isEmpty()) {
      aggregation = (BasicDBObject) results.get(0).get("value");
    }*/
		Statistics stats = new Statistics();
		stats.setAvg(sizeDistribution.getStatistics().get("average").toString());
		stats.setCount( sizeDistribution.getStatistics().get("count").toString() );
		stats.setMax( sizeDistribution.getStatistics().get("max").toString() );
		stats.setMin( sizeDistribution.getStatistics().get("min").toString() );
		stats.setSd( sizeDistribution.getStatistics().get("sd").toString() );
		stats.setSize( sizeDistribution.getStatistics().get("sum").toString() );
		stats.setVar( sizeDistribution.getStatistics().get("var").toString());
		return stats;
	}

	public static Filter getFilterFromQuery( Map<String, String[]> query ) {
		Filter filter = new Filter();

		for ( String key : query.keySet() ) {
			String[] values = query.get( key );

			for ( String val : values ) {
				Object typedValue = Application.getTypedValue( val );
				filter.addFilterCondition( new FilterCondition( key, typedValue ) );
			}
		}

		return filter;
	}

	public static Filter getFilterFromSessionNotSafe() {
		Logger.debug("Getting the filter from the session");

		String session = session(WebAppConstants.SESSION_ID);
		
		return SessionFilters.getFilter(session);

	}
	
	public static Filter getFilterFromSession(){
		 Filter original=getFilterFromSessionNotSafe();
		 Filter newFilter=new Filter();
		 List<FilterCondition> fcs=original.getConditions();
		 for (FilterCondition fc:fcs){
			 newFilter.addFilterCondition(new FilterCondition(fc.getField(),fc.getValue()));
		 }
		 return newFilter;
		 
	}

	/*public static BasicDBObject getFilterQuery(Filter filter) {
		MongoFilterSerializer mfs=new MongoFilterSerializer();
		return null;//mfs.serialize(filter);

	}*/

	private static Graph getFixedWidthHistogram(Filter filter, String property, int width) {
		//BasicDBObject query = FilterController.getFilterQuery(filter);

		final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();

		Property p = pl.getCache().getProperty( property );
		Map<String, Long> hist = pl.getValueHistogramFor(p , filter );

		//MapReduceJob job = new NumericAggregationJob(filter.getCollection(), property);
		//job.setFilterquery(query);

		// MapReduceOutput output = job.execute();
		// List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
		Graph g = null;
		/* if (!hist.isEmpty()) {
      BasicDBObject aggregation = (BasicDBObject) results.get(0).get("value");
      long min = aggregation.getLong("min");
      long max = aggregation.getLong("max");

      int bins = (int) ((max - min) / width);
      Map<String, String> config = new HashMap<String, String>();
      config.put("bin_width", width + "");

      job = new HistogramJob(filter.getCollection(), property);
      job.setFilterquery(query);
      job.setConfig(config);
      output = job.execute();
      results = (List<BasicDBObject>) output.getCommandResult().get("results");
      List<String> keys = new ArrayList<String>();
      List<String> values = new ArrayList<String>();

      calculateNumericHistogramResults(output, keys, values, width);

      g = new Graph(property, keys, values);
    }*/

		return g;

	}

	public static Graph getGraph(Filter filter, String property) {
		
		Distribution d=Application.getDistribution(property, filter);
		Graph g = new Graph( d.getProperty(), d.getPropertyValues(), d.getPropertyValueCounts() );
		g.cutLongTail();
		return g;

		/*  final Graph result = new Graph(property, keys, values);
    final BasicDBObject query = Application.getFilterQuery(filter);
    final MapReduceJob job = new HistogramJob(filter.getCollection(), property, query);

    final Cache cache = Configurator.getDefaultConfigurator().getPersistence().getCache();
    final Property p = cache.getProperty(property);
    long width = -1;

    if (p.getType().equals(PropertyType.INTEGER.toString())) {
      DBObject range = (DBObject) query.get("metadata." + property + ".value");
      if (range==null)
    	  width=50;
      else{
    	  Long low = (Long) range.get("$gte");
    	  Long high = (Long) range.get("$lte");

    	  width = high - low + 1; //because of lte/gte
      }

      HashMap<String, String> config = new HashMap<String, String>();
      config.put("bin_width", width + "");
      job.setConfig(config);

    }

    final MapReduceOutput output = job.execute();
    if (p.getType().equals(PropertyType.INTEGER.toString())) {
      calculateNumericHistogramResults(output, keys, values, width);
    } else {
     calculateHistogramResults(output, keys, values);
    }

    result.sort();

    if (result.getKeys().size() > 100) {
      result.cutLongTail();
    }

    return result;*/
	}

	/*public static Statistics getCollectionStatistics(String name) {
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
  }*/

	/*  public static Statistics getStatisticsFromResult(BasicDBObject aggregation) {
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
  }*/

	public static Graph getGraph(String property) {
		
		DynamicForm form = form().bindFromRequest();
		String alg = form.get("alg");
		//TODO: DEBUG THIS PART!!
		return getGraph(getFilterFromSession(), property);
		
		
		/*Filter filter = FilterController.getFilterFromSession();

		DynamicForm form = form().bindFromRequest();
		String alg = form.get("alg");
		Graph g = null;

		if (alg == null) {
			g = getOrdinalGraph(filter, property);
		} else {
			g = getNumericGraph(filter, property, form.get("alg"), form.get("width"));
		}

		if (g != null) {
			g.sort();

			if (g.getKeys().size() > 100) {
				g.cutLongTail();
			}
		}
		return g;*/
	}

	/*public static Graph getGraph(String collection, String property) {
		final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
		//final List<String> keys = new ArrayList<String>();
		// final List<String> values = new ArrayList<String>();
		//final Graph result = new Graph(property, keys, values);

		    DBCollection dbc = p.getDB().getCollection("histogram_" + collection + "_" + property);

    if (dbc.find().count() == 0) {
      final MapReduceJob job = new HistogramJob(collection, property);
      final MapReduceOutput output = job.execute();

      calculateHistogramResults(output, keys, values);

    } else {
      DBCursor cursor = dbc.find();
      while (cursor.hasNext()) {
        BasicDBObject dbo = (BasicDBObject) cursor.next();
        parseHistogram(dbo, keys, values);
      }
    }
    result.sort();

    if (result.getKeys().size() > 100) {
      result.cutLongTail();
    }
		Filter filter=new Filter();
		filter.addFilterCondition(new FilterCondition("collection", collection));
		Property p = pl.getCache().getProperty( property );
		Map<String, Long> hist = pl.getValueHistogramFor(p , filter );

		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		for ( String k : hist.keySet() ) {
			keys.add( k.replace( "\\", "/" ) );
			values.add( hist.get( k ) + "" );
		}

		Graph g = new Graph( p.getKey(), keys, values );




		return g;
	}*/

	private static Graph getNumericGraph(Filter filter, String property, String alg, String w) {

		// TODO find number of elements based on filter...
		// calculate bins...
		// find classes based on number of bins...
		// map reduce this property based on the classes...
		Graph g = null;
		if (alg.equals("sturge")) {
			// bins = log2 n + 1
			g = getSturgesHistogramm(filter, property);
		} else if (alg.equals("sqrt")) {
			// bins = sqrt(n);
			g = getSquareRootHistogram(filter, property);
		} else {
			alg="fixed";
			int width = 50;
			try {
				width = Integer.parseInt(w);
			} catch (NumberFormatException e) {
				Logger.warn("Not a number, using default bin width: 50");
			}

			g = getFixedWidthHistogram(filter, property, width);
			g.getOptions().put("width", w);


		}

		g.getOptions().put("type", PropertyType.INTEGER.toString());
		g.getOptions().put("alg", alg);


		g.sort();

		return g;
	}

	private static PropertyValuesFilter getNumericValues(String c, Property p, String alg, String width) {
		Filter filter = FilterController.getFilterFromSession();
		Graph graph = null;

		if (alg.equals("fixed")) {
			int w = Integer.parseInt(width);
			graph = getFixedWidthHistogram(filter, p.getId(), w);
		} else if (alg.equals("sqrt")) {
			graph = getSquareRootHistogram(filter, p.getId());
		} else if (alg.equals("sturge")) {
			graph = getSturgesHistogramm(filter, p.getId());
		}

		graph.sort();
		//
		//    if (graph.getKeys().size() > 100) {
		//      graph.cutLongTail();
		//    }

		PropertyValuesFilter f = new PropertyValuesFilter();
		f.setProperty(p.getId());
		f.setType(p.getType());
		f.setValues(graph.getKeys()); // this is not a mistake.

		return f;
	}

	private static Graph getOrdinalGraph(Filter filter, String property) {
		Graph g = null;
		if (filter != null) {
			final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();

			Property p = pl.getCache().getProperty( property );
			Map<String, Long> hist = pl.getValueHistogramFor(p , filter );
			// BasicDBObject ref = new BasicDBObject("descriminator", filter.getDescriminator());
			// DBCursor cursor = Configurator.getDefaultConfigurator().getPersistence().find(Constants.TBL_FILTERS, ref);
			// if (cursor.count() == 1) { // only root filter
			//   g = FilterController.getGraph(filter.getCollection(), property);
			//  } else {
			g = FilterController.getGraph(filter, property);
			// }

		}

		return g;
	}

	private static Graph getSquareRootHistogram(Filter f, String property) {
		//BasicDBObject query = FilterController.getFilterQuery(f);
		//DBCursor cursor = Configurator.getDefaultConfigurator().getPersistence().find(Constants.TBL_ELEMENTS, query);
		//int n = cursor.size();
		//int bins = (int) Math.sqrt(n);
		//MapReduceJob job = new NumericAggregationJob(f.getCollection(), property);
		//job.setFilterquery(query);

		// MapReduceOutput output = job.execute();
		// List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
		Graph g = null;
		/*if (!results.isEmpty()) {
      BasicDBObject aggregation = (BasicDBObject) results.get(0).get("value");
      long max = aggregation.getLong("max");
      int width = (int) (max / bins);
      Map<String, String> config = new HashMap<String, String>();
      config.put("bin_width", width + "");

      job = new HistogramJob(f.getCollection(), property);
      job.setFilterquery(query);
      job.setConfig(config);
      output = job.execute();
      List<String> keys = new ArrayList<String>();
      List<String> values = new ArrayList<String>();

      calculateNumericHistogramResults(output, keys, values, width);

      g = new Graph(property, keys, values);
    }*/

		return g;
	}

	private static Graph getSturgesHistogramm(Filter f, String property) {
		//BasicDBObject query = FilterController.getFilterQuery(f);
		//DBCursor cursor = Configurator.getDefaultConfigurator().getPersistence().find(Constants.TBL_ELEMENTS, query);
		//int n = cursor.size();
		//int bins = (int) ((Math.log(n) / Math.log(2)) + 1);
		//MapReduceJob job = new NumericAggregationJob(f.getCollection(), property);
		//job.setFilterquery(query);

		//MapReduceOutput output = job.execute();
		//List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
		Graph g = null;
		/* if (!results.isEmpty()) {
      BasicDBObject aggregation = (BasicDBObject) results.get(0).get("value");
      long max = aggregation.getLong("max");
      int width = (int) (max / bins);
      Map<String, String> config = new HashMap<String, String>();
      config.put("bin_width", width + "");

      job = new HistogramJob(f.getCollection(), property);
      job.setFilterquery(query);
      job.setConfig(config);
      output = job.execute();
      List<String> keys = new ArrayList<String>();
      List<String> values = new ArrayList<String>();

      calculateNumericHistogramResults(output, keys, values, width);

      g = new Graph(property, keys, values);
    }*/

		return g;
	}

	public static Result getValues() {
		Logger.debug("Received a getValues call in filter");
		final DynamicForm form = form().bindFromRequest();
		final String c = form.get("collection");
		final String p = form.get("filter");

		// get algorithm and width
		final String a = form.get("alg");
		final String w = form.get("width");

		final Cache cache = Configurator.getDefaultConfigurator().getPersistence().getCache();
		final Property property = cache.getProperty(p);
		PropertyValuesFilter f = null;
		if (property.getType().equals(PropertyType.INTEGER.toString())) {
			f = getNumericValues(c, property, a, w); //TODO: Debug this!
		} else {
			f = getValues(c, p, null);
		}
		return ok(play.libs.Json.toJson(f));
	}

	private static PropertyValuesFilter getValues(String c, String p, String v) {
		Logger.debug("get property values filter for " + c + " and property " + p);
		

		Distribution d=Application.getDistribution(p);
		/*Map<String, Long> hist = pl.getValueHistogramFor( p, filter );

		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		for ( String k : hist.keySet() ) {
			keys.add( k.replace( "\\", "/" ) );
			values.add( hist.get( k ) + "" );
		}*/


		/*  if (p.getType().equals(PropertyType.INTEGER)) {
      // int width = (v == null) ? 10 : HistogramJob.inferBinWidth(v);

    	Map<String, Long> hist =pl.getValueHistogramFor(p, filter);




      HashMap<String, String> config = new HashMap<String, String>();
      config.put("bin_width", width + "");
      job.setConfig(config);
    }

    final MapReduceOutput output = job.execute();
    final List<String> keys = new ArrayList<String>();
    final List<String> values = new ArrayList<String>();

    if (p.getType().equals(PropertyType.INTEGER.toString())) {
      // int width = (v == null) ? 10 : HistogramJob.inferBinWidth(v);
      int width = HistogramJob.inferBinWidth(v);

      calculateNumericHistogramResults(output, keys, values, width);
    } else {
      calculateHistogramResults(output, keys, values);
    }
		 */

		PropertyValuesFilter f = new PropertyValuesFilter();
		f.setProperty(d.getProperty());
		f.setType(d.getType());
		f.setValues(d.getPropertyValues()); // this is not a mistake.
		f.setSelected(v);

		return f;
	}

	/*private static void parseHistogram(BasicDBObject dbo, List<String> keys, List<String> values) {
		String key = dbo.getString("_id");
		if (key.endsWith(".0")) {
			key = key.substring(0, key.length() - 2);
		}
		keys.add(key);
		values.add(dbo.getString("value"));
	}*/

	public static Result remove(String property) {
		Logger.debug("Received an add call in filter, removing filter with property " + property);
		Filter filter = FilterController.getFilterFromSession();
		filter.getConditions().remove(property);
		return ok();
	}
}
