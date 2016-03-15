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

import helpers.Distribution;
import helpers.Graph;
import helpers.GraphData;
import helpers.PropertySetTemplate;
import helpers.Statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.overview;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;
import common.WebAppConstants;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.NumericStatistics;

public class Overview extends Controller {

	public static Result index() {
		Logger.debug("Received an index call in overview");
		final List<String> names = Application.getCollectionNames();
		List<Graph> graphs = new ArrayList<Graph>();
		Distribution sizeDistribution=Application.getDistribution("size");
		for (String property: Application.PROPS){
			Distribution d= Application.getDistribution(property);
			Graph g=new Graph(d.getProperty(), d.getPropertyValues(),d.getPropertyValueCounts());
			g.cutLongTail();
			g.sort();
			graphs.add( g );
		}
		GraphData data = new GraphData(graphs);

		Statistics stats = new Statistics();
		stats.setAvg(sizeDistribution.getStatistics().get("average").toString());
		stats.setCount( sizeDistribution.getStatistics().get("count").toString() );
		stats.setMax( sizeDistribution.getStatistics().get("max").toString() );
		stats.setMin( sizeDistribution.getStatistics().get("min").toString() );
		stats.setSd( sizeDistribution.getStatistics().get("sd").toString() );
		stats.setSize( sizeDistribution.getStatistics().get("sum").toString() );
		stats.setVar( sizeDistribution.getStatistics().get("var").toString());



		/*

    Map<String, String[]> queryString = request().queryString();
    Map<String, String[]> query = new HashMap<String, String[]>( queryString );

    String[] histograms = query.remove( "hist" );
    if ( histograms != null && histograms.length != 0 ) {
      List<String> propertyNames = getPropertyNames();
      for ( String h : histograms ) {
        if ( propertyNames.contains( h ) && !h.equals( "mimetype" )) {
          Property property = pl.getCache().getProperty( h );
          graphProperties.add( property );
        }
      }
    }

    Filter filter = Application.getFilterFromQuery( query );

    NumericStatistics statistics = pl.getNumericStatistics( size, filter );
    Statistics stats = new Statistics();
    stats.setAvg( statistics.getAverage() + "" );
    stats.setCount( statistics.getCount() + "" );
    stats.setMax( statistics.getMax() + "" );
    stats.setMin( statistics.getMin() + "" );
    stats.setSd( statistics.getStandardDeviation() + "" );
    stats.setSize( statistics.getSum() + "" );
    stats.setVar( statistics.getVariance() + "" );



    for ( Property p : graphProperties ) {
      Map<String, Long> hist = pl.getValueHistogramFor( p, filter );

      List<String> keys = new ArrayList<String>();
      List<String> values = new ArrayList<String>();
      for ( String k : hist.keySet() ) {
        keys.add( k.replace( "\\", "/" ) );
        values.add( hist.get( k ) + "" );
      }

      Graph g = new Graph( p.getKey(), keys, values );
      g.sort();
      graphs.add( g );
    }
    GraphData data = new GraphData(graphs);
    final List<String> names = Application.getCollectionNames();*/

		return ok(overview.render(names, data, stats, Templates.getCurrentTemplate()));
	}

	public static Result getGraph(String property) {
		Logger.debug("Received a getGraph call for property '" + property + "'");
		// if it is one of the default properties, do not draw..
		for (String p : Application.PROPS) {
			if (p.equals(property)) {
				return ok();
			}
		}

		Graph g = FilterController.getGraph(property);

		return ok(play.libs.Json.toJson(g));
	}

	/* private static GraphData getDefaultGraphs(Filter f, boolean root) {
    List<Graph> graphs = new ArrayList<Graph>();
    for (String prop : Application.PROPS) {
      Graph graph = FilterController.getGraph(f, prop);
      //if (root) {
      //  graph = FilterController.getGraph(f, prop);
      //} else {
      //  graph = FilterController.getGraph(f, prop);
      //}
      graph.cutLongTail();
      graphs.add(graph);

      // TODO decide when to cut long tail...
    }

    return new GraphData(graphs);
  }*/

	/*  private static GraphData getAllGraphs(Filter f, List<String> props) {
    GraphData graphs = getDefaultGraphs(f, false);

    for (String prop : props) {
      boolean found = false;
      for (String def : Application.PROPS) {
        if (prop.equals(def)) {
          found = true;
          break;
        }
      }

      if (!found) {
        Graph graph = FilterController.getGraph(f, prop);
        graphs.getGraphs().add(graph);
      }
    }

    return graphs;
  }*/

}
