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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import views.html.elements;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.utils.Configurator;

/**
 * A play controller for browsing the elements in a batched fashion.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class Elements extends Controller {

  /**
   * Retrieves a list of elements. Depending on the accept header tis method
   * returns html or json or a bad request if the header is not set accordingly.
   * 
   * URL: /c3po/objects <br>
   * Type: GET <br>
   * Parameters: offset (int), limit(init) <br>
   * the 'offset' and 'limit' parameters are optional. If not supplied the first
   * 25 objects are returned.
   * 
   * @return a list of all elements.
   */
  public static Result index() {
    if ( request().accepts( "text/html" ) ) {

      return indexAsHtml();

    } else if ( request().accepts( "application/json" ) ) {

      return indexAsJson();

    } else {

      return badRequest( "Accept Header not supported. Use one of text/html or application/json" );

    }
  }

  /**
   * Retrieves a list of the elements in json format.
   * 
   * @return the list of elements.
   */
  public static Result indexAsJson() {
    List<Element> elements = getElements( request().queryString() );
    response().setContentType( "application/json" );
    return ok( play.libs.Json.toJson( elements ) );
  }

  /**
   * Retrieves a list of the elements in html format.
   * 
   * @return the list of elements.
   */
  private static Result indexAsHtml() {
    Request request = request();
    List<Element> elmnts = getElements( request.queryString() );
    return ok( elements.render( elmnts ) );
  }

  /**
   * Retrieves the correct number of elements matching the query. Reads the
   * passed query for 'offset' and 'limit' parameters and sets them accordingly.
   * If not paramteres are set then the defaults 0 and 25 respectively are used.
   * 
   * @param query
   *          the query parameters from the url.
   * @return the list of elements to return.
   */
  private static ArrayList<Element> getElements( Map<String, String[]> query ) {
    int offset = 0;
    int limit = 25;

    String[] offsetParameters = query.get( "offset" );
    String[] limitParameters = query.get( "limit" );

    offset = readFirstIntegerFromParameterArray( offsetParameters, offset );
    limit = readFirstIntegerFromParameterArray( limitParameters, limit );

    ArrayList<Element> elements = new ArrayList<Element>();
    PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
    Iterator<Element> elementsIterator = persistence.find( Element.class, new Filter() );

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
  }

  /**
   * Reads the first index of the array and tries to convert it to an integer.
   * If the array is null, empty or not integer is found, then the default
   * integer is returned.
   * 
   * @param array
   *          the array to look at.
   * @param def
   *          the default return value
   * @return the integer read from the array.
   */
  private static int readFirstIntegerFromParameterArray( String[] array, int def ) {
    if ( array == null || array.length == 0 ) {
      return def;
    }

    int res = def;
    try {
      res = Integer.parseInt( array[0] );
      if ( res < 0 ) {
        res = def;
      }
    } catch ( NumberFormatException e ) {
      Logger.warn( "Could not parse parameter: " + Arrays.deepToString( array ) );
    }

    return res;
  }

  // public static Result show( String id ) {
  // Logger.info( "Select element with id: " + id );
  // final List<String> names = Application.getCollectionNames();
  // final PersistenceLayer pl =
  // Configurator.getDefaultConfigurator().getPersistence();
  // DBCursor cursor = pl.find( Constants.TBL_ELEMENTS, new BasicDBObject(
  // "_id",
  // new ObjectId( id ) ) );
  //
  // if ( cursor.count() == 0 ) {
  // Logger.info( "Cursor selected " + cursor.count() );
  // return notFound( "No such element exists in the db." );
  // } else if ( cursor.count() > 1 ) {
  // Logger.info( "Cursor selected " + cursor.count() );
  // return notFound( "One or more objects with this id exist" );
  // } else {
  //
  // Element elmnt = DataHelper.parseElement( cursor.next(), pl );
  //
  // return ok( element.render( names, elmnt ) );
  // }
  //
  // }

}
