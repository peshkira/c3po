package controllers;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.utils.Configurator;

import common.WebAppConstants;
import helpers.Distribution;
import helpers.PropertyValuesFilter;
import helpers.SessionFilters;
import play.Logger;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Controller;
import play.data.Form;
import play.mvc.Result;
import views.html.index;

public class PropertyController extends Controller{

	public static String propertiesAsXml() {

		List<String> properties = getPropertyNames();
		//names.add(0, ""); //Adding empty element for default position in the drop-down list
		//names.remove(0);
		final StringBuffer resp = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		resp.append("<properties>\n");

		for (String s : properties) {
			resp.append("<property name=\"" + s + "\" />\n");
		}
		resp.append("</properties>\n");
		response().setContentType("text/xml");
		return resp.toString();
		//return ok(resp.toString());
	}

	public static String collectionsAsXml() {
		final List<String> names = PropertyController.getCollectionNames();
		//names.add(0, ""); //Adding empty element for default position in the drop-down list
		//names.remove(0);
		final StringBuffer resp = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		resp.append("<collections>\n");

		for (String s : names) {
			resp.append("<collection name=\"" + s + "\" />\n");
		}
		resp.append("</collections>\n");
		response().setContentType("text/xml");
		return resp.toString();
		//return ok(resp.toString());
	}

	public static List<String> getCollectionNames() {
		Logger.debug("Listing collection names");
		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		List<String> collections = (List<String>) persistence.distinct(Element.class, "collection", new Filter());
		collections.add(0, "");
		Collections.sort(collections);
		return collections;
	}

	public static Result getCollections() {
		Logger.debug("Received a getCollections call");
		final String accept = request().getHeader("Accept");

		if (accept.contains("*/*") || accept.contains("application/xml")) {
			response().setContentType("text/xml");
			String result=collectionsAsXml();
			return ok(result);
		} else if (accept.contains("application/json")) {
			response().setContentType("application/json");
			List<String> names=PropertyController.getCollectionNames();
			return ok(play.libs.Json.toJson(names));
		}
		return badRequest("The accept header is not supported");
	}
	/*public static List<String> getCollectionNamesToRender() {
		List<String>names=getCollectionNames();
		names.add(0, "");
		return names;
	}*/

	public static Distribution getDistribution(String property, Filter filter){
		Logger.debug("Calculating distrubution for the property '" + property +"'");
		Distribution result=new Distribution();
		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		Property p=persistence.getCache().getProperty(property);
		if (p==null)
			return result;
		if (filter==null)
			filter=new Filter();
		Map<String,Long> histogram=persistence.getValueHistogramFor(p, filter);
		result.setPropertyDistribution(histogram);
		result.setProperty(p.getKey());
		result.setType(p.getType());
		if (p.getType().equals(PropertyType.INTEGER.toString()) || p.getType().equals(PropertyType.FLOAT.toString())){
			Logger.debug("Calculating numeric statistics for the property '" + property +"'");
			NumericStatistics ns=persistence.getNumericStatistics(p, filter);
			result.getStatistics().put("average", ns.getAverage());
			result.getStatistics().put("min", ns.getMin());
			result.getStatistics().put("max", ns.getMax());
			result.getStatistics().put("sd", ns.getStandardDeviation());
			result.getStatistics().put("var", ns.getVariance());
			result.getStatistics().put("count", (double)ns.getCount());
			result.getStatistics().put("sum", ns.getSum());
		}
		return result;
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static Distribution getDistribution(String property){
		Filter f=FilterController.getFilterFromSession();
		return PropertyController.getDistribution(property, f);
	}

	public static Result getProperties() {
		Logger.debug("Received a getProperties call");
		final String accept = request().getHeader("Accept");

		if (accept.contains("*/*") || accept.contains("application/xml")) {
			response().setContentType("text/xml");
			String result=propertiesAsXml();
			return ok(result);
		} else if (accept.contains("application/json")) {
			response().setContentType("application/json");
			List<String> names=PropertyController.getPropertyNames();
			return ok(play.libs.Json.toJson(names));

		}

		return badRequest("The accept header is not supported");
	}

	public static Result getProperty(String name) {
		Logger.debug("Received a getProperty call");
		final String accept = request().getHeader("Accept");
		if (accept.contains("*/*") || accept.contains("application/json")) {
			PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
			Property property = persistence.getCache().getProperty(name);
			return ok(play.libs.Json.toJson(property));
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

	public static Result setCollection(String c) {
		Logger.debug("Received a setCollection call to '" + c + "'");
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
				FilterController.setFilterFromSession(f);
				return ok("The collection was changed successfully");
			}
		}
		session().put(WebAppConstants.CURRENT_COLLECTION_SESSION, c);
		f.addFilterCondition(new FilterCondition("collection", c));
		FilterController.setFilterFromSession(f);
		return ok("The collection was changed successfully");
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

	public static Result getValues() {
		Logger.debug("Received a getValues call in filter");
		final DynamicForm form = play.data.Form.form().bindFromRequest();
		final String c = form.get("collection");
		final String p = form.get("filter");

		// get algorithm and width
		final String a = form.get("alg");
		final String w = form.get("width");

		PropertyValuesFilter f = null;
		//if (property.getType().equals(PropertyType.INTEGER.toString())) {
		//	f = getNumericValues(c, property, a, w); //TODO: Debug this!
		//} else {
		f = getValues( p, null);
		//}
		return ok(play.libs.Json.toJson(f));
	}

	public static PropertyValuesFilter getValues(String p, String v) {
		Logger.debug("get property values filter for property " + p);
		if (p.equals("collection")){
			PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
			Property property=persistence.getCache().getProperty(p);
			PropertyValuesFilter pvf=new PropertyValuesFilter();
			pvf.setProperty(property.getKey());
			pvf.setType(property.getType());
			List<String> collections=getCollectionNames();
			collections.remove(0);
			pvf.setValues(collections);
			pvf.setSelected(getCollection());
			return pvf;
		}
		else{
			Distribution d=getDistribution(p, null);
			PropertyValuesFilter pvf = new PropertyValuesFilter();
			pvf.setProperty(d.getProperty());
			pvf.setType(d.getType());
			pvf.setValues(d.getPropertyValues());
			pvf.setSelected(v);

			return pvf;
		}
	}







}
