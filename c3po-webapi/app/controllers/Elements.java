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

import org.bson.types.ObjectId;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.element;
import views.html.elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
//import com.petpet.c3po.datamodel.Element;
//import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;
import common.WebAppConstants;


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
		List<String> names = Application.getCollectionNames();
		String collection = Application.getCollection();
		if (collection == null) {
			return ok(elements.render(names, null));
		}
		int batch = getQueryParameter("batch", 25);
		int offset = getQueryParameter("offset", 0);

		Filter filter = FilterController.getFilterFromSession();
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

		//List<Element> result = listElements(collection, batch, offset);
		return ok(elements.render(names, result));//listElements(collection, batch, offset);

	}

	/*	public static List<Element> listElements(String collection, int batch, int offset) {

		List<Element> result = new ArrayList<Element>();
		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();

		//BasicDBObject query = new BasicDBObject();
		//query.put("collection", collection);
		Filter filter = FilterController.getFilterFromSession();
		//if (filter != null) {
		//  query = Application.getFilterQuery(filter);
		//  System.out.println("Objects Query: " + query);
		//}

		//final DBCursor cursor = pl.getDB().getCollection(Constants.TBL_ELEMENTS).find(query).skip(offset).limit(batch);

		//Logger.info("Cursor has: " + cursor.count() + " objects");
		Iterator<Element> iterator=persistence.find(Element.class, filter);
		while (iterator.hasNext()) {
			Element e = iterator.next();

			if (e.getName() == null) {
				e.setName("missing name");
			}
			result.add(e);
		}
		//return result;




		    Map<String, String[]> urlQuery = new HashMap<String, String[]>();
		    urlQuery.putAll( query );

		    String[] offsetParameters = urlQuery.remove( "offset" );
		    String[] limitParameters = urlQuery.remove( "limit" );
		    Filter filter = Application.getFilterFromQuery( urlQuery );

		    offset = readFirstIntegerFromParameterArray( offsetParameters, offset );
		    limit = readFirstIntegerFromParameterArray( limitParameters, limit );

		    ArrayList<Element> elements = new ArrayList<Element>();
		    PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		    Iterator<Element> elementsIterator = persistence.find( Element.class, filter );

		    while ( offset > 0 ) {
		      if ( elementsIterator.hasNext() ) {
		        elementsIterator.next();
		        offset--;
		      } else {
		        break;
		      }
		    }

		    while ( limit > 0 ) {
		      if ( elementsIterator.hasNext() ) {
		        Element element = elementsIterator.next();
		        elements.add( element );
		        limit--;
		      } else {
		        break;
		      }

		    }

		    return elements;

	}*/

	public static Result show(String id) {
		Logger.debug("Received a show call in elements, with id: " + id);
		List<String> names = Application.getCollectionNames();
		PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
		Filter filter=FilterController.getFilterFromSession();

		filter.addFilterCondition(new FilterCondition("_id", new ObjectId(id) ));
		//DBCursor cursor =pl.find(Element.class, filter);// arg1) pl.find(Constants.TBL_ELEMENTS, new BasicDBObject("_id", new ObjectId(id)));

		Iterator<Element> iterator=persistence.find(Element.class, filter);


		if (iterator.hasNext()) {
			Element result = iterator.next();
			if (iterator.hasNext()) {
				return internalServerError( "There were two or more elements with the given unique identifier: " + id );
			}

			return ok( element.render(names, result) );

		} else {
			return notFound( "{error: \"Element not found\"}" ) ;
		}


	}

}
