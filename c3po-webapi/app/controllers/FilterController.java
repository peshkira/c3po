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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
import play.data.Form;
import play.mvc.Result;

public class FilterController extends Controller {

	public static Result addCondition() {
		Logger.debug("Received an add call in filter");
		PersistenceLayer persistence=Configurator.getDefaultConfigurator().getPersistence();
		// final List<String> names = Application.getCollectionNames();
		Filter filter = FilterController.getFilterFromSession();
		if (filter != null) {
			DynamicForm form = play.data.Form.form().bindFromRequest();
			String propertyName = form.get("filter");
			String propertyValueString = form.get("value");
			String t = form.get("type");
			String a = form.get("alg");
			String w = form.get("width");
			if (t.equals("graph")) {
				int value = Integer.parseInt(propertyValueString);
				Graph graph = Graph.getGraph(filter, propertyName);
				graph.cutLongTail();
				propertyValueString = graph.getKeys().get(value);
			}
			Object propertyValue=null;
			Property p=persistence.getCache().getProperty(propertyName);
			if (p.getType().equals(PropertyType.INTEGER.toString()))
			{
				if (propertyValueString.equals("Unknown"))
					propertyValue=null;
				else if (propertyValueString.equals("Conflicted"))
					propertyValue="CONFLICT";
				else
					propertyValue=Integer.parseInt(propertyValueString);
			}
			else if (p.getType().equals(PropertyType.FLOAT.toString()))
			{
				if (propertyValueString.equals("Unknown") )
					propertyValue=null;
				else if (propertyValueString.equals("Conflicted"))
					propertyValue="CONFLICT";
				else
					propertyValue=Long.parseLong(propertyValueString);
			}
			else if (p.getType().equals(PropertyType.BOOL.toString()))
			{
				if (propertyValueString.equals("Unknown") )
					propertyValue=null;//propertyValueString;
				else if (propertyValueString.equals("Conflicted"))
					propertyValue="CONFLICT";
				else
					propertyValue=Boolean.parseBoolean(propertyValueString);
			}
			else if (p.getType().equals(PropertyType.STRING.toString()))
			{
				if (propertyValueString.equals("Unknown"))
					propertyValue=null;//propertyValueString;
				else if (propertyValueString.equals("Conflicted"))
					propertyValue="CONFLICT";
				else
					propertyValue=propertyValueString;
			}
			else if (p.getType().equals(PropertyType.DATE.toString()))
			{
				if (propertyValueString.equals("Unknown") )
					propertyValue=null;
				else if (propertyValueString.equals("Conflicted"))
					propertyValue="CONFLICT";
				else {
					DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");
					try {
						propertyValue = dateFormat.parse(propertyValueString);
					} catch (ParseException e) {
						propertyValue = propertyValueString;
					}
				}
			}

			List<FilterCondition> fcs=filter.getConditions();
			for (FilterCondition fc: fcs){
				if (fc.getField().equals(propertyName)){
					fc.setValue(propertyValue);
					FilterController.setFilterFromSession(filter);
					return ok();
				}
			}

			filter.addFilterCondition(new FilterCondition(propertyName,propertyValue));
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
			Object obj=fc.getValue();
			String v="Unknown";
			if (obj!=null){
				v=obj.toString();
			}
			PropertyValuesFilter f = PropertyController.getValues( p.getKey(), v);
			f.setSelected(v);
			if (v.equals("CONFLICT")){
				f.setSelected("Conflicted");
			}
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
		PersistenceLayer persistence=Configurator.getDefaultConfigurator().getPersistence();

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

		DynamicForm form = play.data.Form.form().bindFromRequest();
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
