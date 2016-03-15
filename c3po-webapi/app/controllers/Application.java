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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.utils.Configurator;

import common.WebAppConstants;
import helpers.Distribution;
import helpers.SessionFilters;
import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {

	public static String[] PROPS = { "mimetype", "format", "format_version", "valid", "wellformed",
			"creating_application_name", "created" };

	private static void buildSession() {
		String session = session(WebAppConstants.SESSION_ID);
		Logger.debug("Building a new session with id:'" + session + "'");
		if (session == null) {
			session(WebAppConstants.SESSION_ID, UUID.randomUUID().toString());
		}
		Filter f=new Filter();
		SessionFilters.addFilter(session, f);
	}

	public static Result clear() {
		Logger.debug("Received a clear call");
		session().clear();

		//final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
		//pl.remove(arg0);

		return redirect("/c3po");
	}

	public static Result collectionsAsJson() {
		Logger.debug("Received a collectionsAsJson call");
		final List<String> names = Application.getCollectionNames();
		names.add(0, ""); //Adding empty element for default position in the drop-down list
		//names.remove(0);
		response().setContentType("application/json");
		return ok(play.libs.Json.toJson(names));
	}

	public static Result collectionsAsXml() {
		Logger.debug("Received a collectionsAsXml call");
		final List<String> names = Application.getCollectionNames();
		names.add(0, ""); //Adding empty element for default position in the drop-down list
		//names.remove(0);
		final StringBuffer resp = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		resp.append("<collections>\n");

		for (String s : names) {
			resp.append("<collection name=\"" + s + "\" />\n");
		}
		resp.append("</collections>\n");
		response().setContentType("text/xml");
		return ok(resp.toString());
	}

	public static List<String> getCollectionNames() {
		Logger.debug("Listing collection names");
		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		List<String> collections = (List<String>) persistence.distinct(Element.class, "collection", null);
		Collections.sort(collections);
		return collections;
	}

	public static Result getCollections() {
		Logger.debug("Received a getCollections call");
		final String accept = request().getHeader("Accept");

		if (accept.contains("*/*") || accept.contains("application/xml")) {
			return collectionsAsXml();
		} else if (accept.contains("application/json")) {
			return collectionsAsJson();
		}

		return badRequest("The accept header is not supported");
	}

	
	
	public static Distribution getDistribution(String property, Filter filter){
		Logger.debug("Calculating distrubution for the property '" + property +"'");
		Distribution result=new Distribution();
		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		Property p=persistence.getCache().getProperty(property);
		if (p==null)
			return result;
		Filter f=filter;
		result.setPropertyDistribution( persistence.getValueHistogramFor(p, f));
		result.setProperty(p.getKey());
		result.setType(p.getType());
		switch (p.getType()) {
		case "STRING":

			break;
		case "BOOL":

			break;
		case "INTEGER":
		{
			Logger.debug("Calculating numeric statistics for the property '" + property +"'");
			NumericStatistics ns=persistence.getNumericStatistics(p, f);
			result.getStatistics().put("average", ns.getAverage());
			result.getStatistics().put("min", ns.getMin());
			result.getStatistics().put("max", ns.getMax());
			result.getStatistics().put("sd", ns.getStandardDeviation());
			result.getStatistics().put("var", ns.getVariance());
			result.getStatistics().put("count", (double)ns.getCount());
			result.getStatistics().put("sum", ns.getSum());
		}
		break;
		case "FLOAT":
			Logger.debug("Calculating numeric statistics for the property '" + property +"'");
			NumericStatistics ns=persistence.getNumericStatistics(p, f);
			result.getStatistics().put("average", ns.getAverage());
			result.getStatistics().put("min", ns.getMin());
			result.getStatistics().put("max", ns.getMax());
			result.getStatistics().put("sd", ns.getStandardDeviation());
			result.getStatistics().put("var", ns.getVariance());
			result.getStatistics().put("count", (double)ns.getCount());
			result.getStatistics().put("sum", ns.getSum());
			break;
		case "DATE":
			break;
		case "ARRAY":
			break;
		default:
			break;
		}
		return result;
	}
	
	public static Distribution getDistribution(String property){
		Filter f=FilterController.getFilterFromSession();
		return Application.getDistribution(property, f);
	}

	public static Result getProperties() {
		Logger.debug("Received a getProperties call");
		final String accept = request().getHeader("Accept");

		if (accept.contains("*/*") || accept.contains("application/xml")) {
			return TODO;
		} else if (accept.contains("application/json")) {
			return propertiesAsJson();
		}

		return badRequest("The accept header is not supported");
	}

	public static Result getProperty(String name) {
		Logger.debug("Received a getProperty call");
		final String accept = request().getHeader("Accept");
		if (accept.contains("*/*") || accept.contains("application/json")) {
			return propertyAsJson(name);
		} else {
			return TODO;
		}
	}

	public static List<String> getPropertyNames() {
		Logger.debug("Listing property names");
		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		List<String> names = persistence.distinct( Property.class, "_id", null );
		return names;
	}

	public static Result getSetting(String key) {
		Logger.debug("Received a getSetting call");
		String value = session(key);
		return ok(play.libs.Json.toJson(value));
	}

	static Object getTypedValue( String val ) {
		Logger.debug("Retrieving typedValue of '" +val+ "'");
		Object value = null;
		try {
			value = Long.parseLong( val );
		} catch ( NumberFormatException e ) {
		}

		if ( val.equalsIgnoreCase( "yes" ) || val.equalsIgnoreCase( "true" ) ) {
			value = new Boolean( true );
		} else if ( val.equalsIgnoreCase( "no" ) || val.equalsIgnoreCase( "false" ) ) {
			value = new Boolean( false );
		}

		if ( value == null ) {
			value = val;
		}
		return value;
	}

	public static Result index() {
		Logger.debug("Received an index call in application");
		buildSession();
		return ok(index.render("c3po", getCollectionNames()));
	}

	private static Object inferValue(String value) {
		Logger.debug("Inferring value of '" +value+ "'");
		Object result = value;
		if (value.equalsIgnoreCase("true")) {
			result = new Boolean(true);
		}

		if (value.equalsIgnoreCase("false")) {
			result = new Boolean(false);
		}

		return result;
	}

	private static Result propertiesAsJson() {
		Logger.debug("Received a propertiesAsJson call");
		//PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		List<String> properties = getPropertyNames();
		//Iterator<Property> iter = persistence.find( Property.class, null );
		//while ( iter.hasNext() ) {
		//		properties.add( iter.next().getKey() );
		//	}

		return ok( play.libs.Json.toJson( properties ) );
	}

	private static Result propertyAsJson(String name) {
		Logger.debug("Received a propertyAsJson call");
		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		Property property = persistence.getCache().getProperty(name);
		return ok(play.libs.Json.toJson(property));
	}



	public static Result setCollection(String c) {
		Logger.debug("Received a setCollection call to '" + c + "'");
		final PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
		final List<String> names = getCollectionNames();

		if (c == null || c.equals("") || !names.contains(c)) {
			return notFound("No collection '" + c + "' was found");
		}
		
		
		Filter f = FilterController.getFilterFromSession();  //I dont use getCollection(), because we need to update the collection value in the filter.
		List<FilterCondition> fcs=f.getConditions();
		for (FilterCondition fc: fcs){
			if (fc.getField().equals("collection")){
				fc.setValue(c);
				session().put(WebAppConstants.CURRENT_COLLECTION_SESSION, c);
				return ok("The collection was changed successfully\n");
			}
		}
		f.addFilterCondition(new FilterCondition("collection", c));
		//f.addFilterCondition(new FilterCondition("collection", c));
		//buildSession();
		//String session = session(WebAppConstants.SESSION_ID);
		//SessionFilters.addFilter(session, f);
		//System.out.println("session: " + session);

		//session().put(WebAppConstants.CURRENT_COLLECTION_SESSION, c);
		return ok("The collection was changed successfully\n");
	}
	
	public static String getCollection(){
		Filter f = FilterController.getFilterFromSession();
		List<FilterCondition> fcs=f.getConditions();
		for (FilterCondition fc: fcs){
			if (fc.getField().equals("collection")){
				return fc.getValue().toString();
			}
		}
		return null;

	}

	public static Result setSetting() {

		DynamicForm form = form().bindFromRequest();
		String setting = form.get("setting");
		String value = form.get("value");
		Logger.debug("Received a setSetting call for '" + setting + "' to value: '" + value + "'");
		session().put(setting, value);

		return ok();
	}



}
