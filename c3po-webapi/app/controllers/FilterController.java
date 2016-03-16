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
import java.util.Iterator;
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

	public static Result addCondition() {
		Logger.debug("Received an add call in filter");
		// final List<String> names = Application.getCollectionNames();
		Filter filter = FilterController.getFilterFromSession();
		if (filter != null) {
			final DynamicForm form = form().bindFromRequest();
			final String propertyName = form.get("filter");
			final String propertyValue = form.get("value");
			final String t = form.get("type");
			final String a = form.get("alg");
			final String w = form.get("width");

			if (t == null || t.equals("normal")) {
				List<FilterCondition> fcs=filter.getConditions();
				for (FilterCondition fc: fcs){
					if (fc.getField().equals(propertyName)){
						fc.setValue(propertyValue);
						FilterController.setFilterFromSession(filter);
						return ok();
					}
				}
				
				filter.addFilterCondition(new FilterCondition(propertyName,propertyValue));
			} else if (t.equals("graph")) {
				int value = Integer.parseInt(propertyValue);
				Graph graph = null;
				graph = Graph.getGraph(filter, propertyName);
				graph.cutLongTail();
				final String filtervalue = graph.getKeys().get(value);
	
				List<FilterCondition> fcs=filter.getConditions();
				for (FilterCondition fc: fcs){
					if (fc.getField().equals(propertyName)){
						fc.setValue(filtervalue);
						FilterController.setFilterFromSession(filter);
						return ok();
					}
				}
				filter.addFilterCondition(new FilterCondition(propertyName,filtervalue));
			}
			FilterController.setFilterFromSession(filter);
			return ok();
		}

		return badRequest("No filter was found in the session\n");
	}	

	/**
	 * Gets all selected filters and returns them to the client, so that it can
	 * reconstruct the page.
	 * 
	 * @return
	 */
	public static Result getConditions() {
		Logger.debug("Received a getAll call in filter");
		PersistenceLayer persistence=Configurator.getDefaultConfigurator().getPersistence();
		List<PropertyValuesFilter> result = new ArrayList<PropertyValuesFilter>();
		Filter filter = FilterController.getFilterFromSession();
		List<FilterCondition> fcs=filter.getConditions();
		for(FilterCondition fc: fcs){
			Property p=persistence.getCache().getProperty(fc.getField());
			//String c=PropertyController.getCollection();
			String v=fc.getValue().toString();
			PropertyValuesFilter f = PropertyController.getValues( p.getKey(), v);
			f.setSelected(v);
			result.add(f);
		}
		return ok(play.libs.Json.toJson(result));
	}

	public static Statistics getCollectionStatistics(Filter filter) {
		Distribution sizeDistribution = PropertyController.getDistribution("size",filter);
		final PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		Property size =  persistence.getCache().getProperty( "size" );  
		NumericStatistics statistics =  persistence.getNumericStatistics( size, filter );

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

	public static Filter getFilterFromSession() {
		String session = session(WebAppConstants.SESSION_ID);
		return SessionFilters.getFilter(session);

	}

	public static void setFilterFromSession(Filter filter){
		String session = session(WebAppConstants.SESSION_ID);
		SessionFilters.addFilter(session, filter);
	}
	
	private static PropertyValuesFilter getNumericValues(String c, Property p, String alg, String width) {
		Filter filter = FilterController.getFilterFromSession();
		Graph graph = null;

		if (alg.equals("fixed")) {
			int w = Integer.parseInt(width);
			graph = Graph.getFixedWidthHistogram(filter, p.getId(), w);
		} else if (alg.equals("sqrt")) {
			graph = Graph.getSquareRootHistogram(filter, p.getId());
		} else if (alg.equals("sturge")) {
			graph = Graph.getSturgesHistogramm(filter, p.getId());
		}

		graph.sort();

		PropertyValuesFilter f = new PropertyValuesFilter();
		f.setProperty(p.getId());
		f.setType(p.getType());
		f.setValues(graph.getKeys()); // this is not a mistake.

		return f;
	}
	public static Graph getGraph(String property) {

		DynamicForm form = form().bindFromRequest();
		String alg = form.get("alg");
		//TODO: DEBUG THIS PART!!
		return Graph.getGraph(FilterController.getFilterFromSession(), property);


		
	}

	public static Result removeCondition(String property) {
		Logger.debug("Received a removeCondition call in filter, removing filter with property " + property);
		Filter filter = FilterController.getFilterFromSession();
		for (Iterator<FilterCondition> iter = filter.getConditions().listIterator(); iter.hasNext(); ) {
			FilterCondition fc = iter.next();
			if (fc.getField().equals(property)) {
		        iter.remove();
		    }
		}
		FilterController.setFilterFromSession(filter);
		if (property.equals("collection")){
			session().put(WebAppConstants.CURRENT_COLLECTION_SESSION, "none");
			
		}
		return ok();
	}
}
