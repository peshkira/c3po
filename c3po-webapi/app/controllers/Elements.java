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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import org.bson.types.ObjectId;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.element;
import views.html.elements;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.utils.Configurator;


import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
public class Elements extends Controller {

	private static int getQueryParameter(String parameter, int dflt) {
		String[] strings = request().queryString().get(parameter);
		if (strings == null || strings.length == 0) {
			return dflt;
		}
		return Integer.parseInt(strings[0]);
	}

	public static Result index() {
		Logger.debug("Received an index call in elements");
		List<String> names = Properties.getCollectionNames();

		int batch = getQueryParameter("batch", 25);
		int offset = getQueryParameter("offset", 0);

		Filter filter = Filters.getFilterFromSession();
		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		Iterator<Element> elementsIterator = persistence.find( Element.class, filter );
		List<Element> result=new ArrayList<Element>();
		while ( offset > 0 ) {
			if ( elementsIterator.hasNext() ) {
				elementsIterator.next();
				offset--;
			} else {
				break;
			}
		}

		while ( batch > 0 ) {
			if ( elementsIterator.hasNext() ) {
				Element element = elementsIterator.next();
				result.add( element );
				batch--;
			} else {
				break;
			}

		}
		return ok(elements.render(names, result));
	}



	public static Result show(String id) {
		Logger.debug("Received a show call in elements, with id: " + id);
		List<String> names = Properties.getCollectionNames();
		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		Filter filter=new Filter();
		filter.addFilterCondition(new FilterCondition("_id", new ObjectId(id) ));
		Iterator<Element> iterator=persistence.find(Element.class, filter);
		if (iterator.hasNext()) {
			Element result = iterator.next();
			List<MetadataRecord> metadata = result.getMetadata();
			if (iterator.hasNext()) {
				return internalServerError( "There were two or more elements with the given unique identifier: " + id );
			}
			return ok( element.render(names, result) );
		} else {
			return notFound( "{error: \"Element not found\"}" ) ;
		}

	}

	public static Result get(String id) {
		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		Filter filter=new Filter();
		filter.addFilterCondition(new FilterCondition("_id", new ObjectId(id) ));
		Iterator<Element> iterator=persistence.find(Element.class, filter);
		if (iterator.hasNext()) {
			Element result = iterator.next();
			if (iterator.hasNext()) {
				return internalServerError( "There were two or more elements with the given unique identifier: " + id );
			}
			return ok(elementToJSON(result));
		} else {
			return notFound( "{error: \"Element not found\"}" ) ;
		}
	}

	public static Result getRaw(String id) {
		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		Filter filter=new Filter();
		filter.addFilterCondition(new FilterCondition("_id", new ObjectId(id) ));
		Iterator<Element> iterator=persistence.find(Element.class, filter);
		if (iterator.hasNext()) {
			Element result = iterator.next();
			if (iterator.hasNext()) {
				return internalServerError( "There were two or more elements with the given unique identifier: " + id );
			}
			return ok(play.libs.Json.toJson(result));
		} else {
			return notFound( "{error: \"Element not found\"}" ) ;
		}
	}

	private static ArrayNode elementToJSON(Element element){

		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		Iterator<Source> sourceIterator = persistence.find(Source.class, null);
		List<String> sourceNames=new ArrayList<String>();
		while (sourceIterator.hasNext()){
			Source next = sourceIterator.next();
			sourceNames.add(next.toString());
		}
		List<MetadataRecord> metadata = element.getMetadata();
		ArrayNode result = new ArrayNode(new JsonNodeFactory(false));
		String nullStr=null;
		for(MetadataRecord mr: metadata){
			List<String> values = mr.getValues();
			List<String> sources = mr.getSources();
			String propertyKey = mr.getProperty();
			Property property = persistence.getCache().getProperty(propertyKey);

			ObjectNode tmp=Json.newObject();

			tmp.put("Property", property.getKey());

			tmp.put("Status", mr.getStatus());
			for (Map.Entry<String, String> stringStringEntry : mr.getSourcedValues().entrySet()) {
				String source = stringStringEntry.getKey();
				String value = stringStringEntry.getValue();
			}
			for (String sourceName : sourceNames) {
				if (sources.contains(sourceName)){
					tmp.put(sourceName,mr.getSourcedValues().get(sourceName));
				} else {
					tmp.put(sourceName,nullStr);
				}
			}

			result.add(tmp);
		}
		return result;




	}

	public static Result uploadFile() {
		try {
			Http.MultipartFormData body = request().body().asMultipartFormData();
			Http.MultipartFormData.FilePart fileP = body.getFile("file");
			if (fileP != null) {
				File file = fileP.getFile();
				System.out.println(file.getName());
				com.petpet.c3po.controller.Controller.processFast(file, "uploaded");  //TODO: this is broken...
				return ok();
			} else {
				return badRequest("Upload Error");
			}
		} catch (Exception e){
			return badRequest("Upload Error");
		}
	}

}
