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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.samples;

import com.petpet.c3po.analysis.RepresentativeAlgorithmFactory;
import com.petpet.c3po.analysis.RepresentativeGenerator;
import com.petpet.c3po.api.dao.PersistenceLayer;
//import com.petpet.c3po.datamodel.Element;
//import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;


import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;

public class Samples extends Controller {

  public static Result index() {
    final List<String> names = Properties.getCollectionNames();
    return ok(samples.render(names));
  }

  public static Result getSamples(String alg, int size, String props) {
	Logger.debug("Received a getSamples call, sampling with alg " + alg + " size " + size + " props " + props);
    //final Configurator configurator = Configurator.getDefaultConfigurator();
    final PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
    final Filter filter = Filters.getFilterFromSession();
    final RepresentativeGenerator sg = new RepresentativeAlgorithmFactory().getAlgorithm(alg);
    sg.setFilter(filter);

    if (alg.equals("distsampling")) {
      final String[] properties = props.split(",");
      final HashMap<String, Object> options = new HashMap<String, Object>();
      options.put("properties", Arrays.asList(properties));
      sg.setOptions(options);
    }

    List<Element> samples = new ArrayList<Element>();
    if (filter != null) {
      List<String> output = sg.execute(size);
      
      for (String id: output){
    	  Filter idFilter = new Filter();
    	  idFilter.addFilterCondition( new FilterCondition("_id", new ObjectId(id) ) ) ;
    	  Iterator<Element> iter = persistence.find( Element.class, idFilter );
    	  samples.add(iter.next());
      }
     /* BasicDBObject query = new BasicDBObject("uid", new BasicDBObject("$in", output));
      //DBCursor cursor = pl.find(Constants.TBL_ELEMENTS, query);
      Filter idFilter = new Filter();
      idFilter.addFilterCondition( new FilterCondition( new FilterCondition("_id", new ObjectId(id) ) ) );
      
      for ( String id : output ) {
	      idFilter.addFilterCondition( new FilterCondition( "uid", id ) );
	    }
      
      Iterator<Element> iter = pl.find( Element.class, idFilter );
      
      while (iter.hasNext()) {
        samples.add(iter.next());
      }*/
    }

    return ok(play.libs.Json.toJson(samples));
  }
  
  
 /*
  
  private static Result indexAsHtml() {
	    Map<String, String[]> queryString = request().queryString();
	    Map<String, String[]> query = new HashMap<String, String[]>();
	    query.putAll( queryString );

	    String[] algArray = query.remove( "alg" );
	    if ( algArray == null || algArray.length == 0 ) {
	      return ok( samples.render( null, null ) );
	    }

	    String alg = algArray[0];

	    if ( !RepresentativeAlgorithmFactory.isValidAlgorithm( alg ) ) {
	      return ok( samples.render( null,
	          "The provided algorithm is not supported. Use one of 'sizesampling', 'syssampling', 'distsampling'" ) );
	    }

	    String[] countArray = query.remove( "count" );
	    if ( countArray == null || countArray.length == 0 ) {
	      return ok( samples.render( null, "No count parameter provided" ) );
	    }
	    int size = -1;
	    try {
	      size = Integer.valueOf( countArray[0] );
	    } catch ( NumberFormatException e ) {
	      return ok( samples.render( null, "The provided count parameter is invalid. Please provide a positive integer" ) );
	    }

	    if ( size <= 0 ) {
	      return ok( samples.render( null, "The provided count parameter is invalid. Please provide a positive integer" ) );
	    }

	    RepresentativeGenerator gen = new
	        RepresentativeAlgorithmFactory().getAlgorithm( alg );

	    if ( alg.equals( "distsampling" ) ) {
	      String[] propertyArray = query.remove( "property" );
	      if ( propertyArray == null || propertyArray.length == 0 ) {
	        return ok( samples.render( null, "Cannot use distsampling without definining at least one property" ) );
	      }

	      final HashMap<String, Object> options = new HashMap<String, Object>();
	      options.put( "properties", Arrays.asList( propertyArray ) );
	      gen.setOptions( options );
	    }

	    List<Element> smpls = getSamples( gen, query, size );
	    return ok( samples.render( smpls, null ) );

	  }
*/
	  public static Result indexAsJson() {
	    response().setContentType( "application/json" );
	    Map<String, String[]> queryString = request().queryString();
	    Map<String, String[]> query = new HashMap<String, String[]>();
	    query.putAll( queryString );

	    String[] algArray = query.remove( "alg" );
	    if ( algArray == null || algArray.length == 0 ) {
	      return badRequest( "{error: \"No algorithm (alg) parameter provided. Use one of 'sizesampling', 'syssampling', 'distsampling'\"}" );
	    }

	    String alg = algArray[0];

	    if ( !RepresentativeAlgorithmFactory.isValidAlgorithm( alg ) ) {
	      return badRequest( "{error: \"The provided algorithm is not supported. Use one of 'sizesampling', 'syssampling', 'distsampling'\"}" );
	    }

	    String[] countArray = query.remove( "count" );
	    if ( countArray == null || countArray.length == 0 ) {
	      return badRequest( "{error: \"No count parameter provided\"}" );
	    }
	    int size = -1;
	    try {
	      size = Integer.valueOf( countArray[0] );
	    } catch ( NumberFormatException e ) {
	      return badRequest( "{error: \"The provided count parameter is invalid. Please provide a positive integer\"}" );
	    }

	    if ( size <= 0 ) {
	      return badRequest( "{error: \"The provided count parameter is invalid. Please provide a positive integer\"}" );
	    }

	    RepresentativeGenerator gen = new
	        RepresentativeAlgorithmFactory().getAlgorithm( alg );

	    if ( alg.equals( "distsampling" ) ) {
	      String[] propertyArray = query.remove( "property" );
	      if ( propertyArray == null || propertyArray.length == 0 ) {
	        return badRequest( "{error: \"Cannot use distsampling without definining at least one property\"}" );
	      }

	      final HashMap<String, Object> options = new HashMap<String, Object>();
	      options.put( "properties", Arrays.asList( propertyArray ) );
	      gen.setOptions( options );
	    }

	    List<Element> samples = getSamples( gen, query, size );
	    return ok( play.libs.Json.toJson( samples ) );
	  }

	  private static List<Element> getSamples( RepresentativeGenerator gen, Map<String, String[]> query, int count ) {
	    Configurator configurator = Configurator.getDefaultConfigurator();
	    PersistenceLayer pl = configurator.getPersistence();
	    Filter filter = Filters.getFilterFromQuery( query );
	    gen.setFilter( filter );

	    List<String> ids = gen.execute( count );
	    System.out.println( ids.size() );
	    Filter idFilter = new Filter();
	    for ( String id : ids ) {
	      idFilter.addFilterCondition( new FilterCondition( "uid", id ) );
	    }

	    Iterator<Element> iter = pl.find( Element.class, idFilter );
	    List<Element> elements = new ArrayList<Element>();
	    while ( iter.hasNext() ) {
	      elements.add( iter.next() );
	    }

	    return elements;
	  }
  

}
