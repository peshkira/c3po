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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.export;

import com.petpet.c3po.analysis.CSVGenerator;
import com.petpet.c3po.analysis.ProfileGenerator;
import com.petpet.c3po.analysis.RepresentativeAlgorithmFactory;
import com.petpet.c3po.analysis.RepresentativeGenerator;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.utils.Configurator;

public class Export extends Controller {

  public static Result index() {
    return ok( export.render() );
  }

  // TODO add analysis action log
  // TODO check if last action log is analysis
  // and if yes, check if the export file already exists.
  // For this the name should be changed by the hash of filter or similar.
  // If it exists, then return the file and if not regenerate...
  public static Result exportToCSV() {
    if ( request().accepts( "text/csv" ) ) {
      Map<String, String[]> queryString = request().queryString();
      Map<String, String[]> query = new HashMap<String, String[]>( queryString );

      String[] properties = query.remove( "property" );
      Filter filter = Application.getFilterFromQuery( query );

      // TODO this should be done via the controller
      PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
      CSVGenerator gen = new CSVGenerator( persistence );

      SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd_HHmm" );
      String name = "c3po_export_" + sdf.format( new Date() ) + ".csv";
      String path = "csv" + File.separator + name;
      if ( properties == null || properties.length == 0 ) {
        gen.export( filter, name );
      } else {
        List<Property> props = new ArrayList<Property>();
        for ( String p : properties ) {
          props.add( persistence.getCache().getProperty( p ) );
        }
        gen.export( filter, props, path );
      }

      try {
        response().setContentType( "application/force-download" );
        response().setHeader( "Content-Transfer-Encoding", "binary" );
        response().setHeader( "Content-Disposition", "attachment; filename=\"" + name + "\"" );
        return ok( new FileInputStream( new File( path ) ) );
      } catch ( FileNotFoundException e ) {
        return internalServerError( e.getMessage() );
      }

    } else {
      return badRequest( "The provided accept header is not supported: " + request().getHeader( "Accept" ) );
    }
  }

  public static Result profileToXML() {
    if ( request().accepts( "text/xml" ) ) {
      Map<String, String[]> queryString = request().queryString();
      Map<String, String[]> query = new HashMap<String, String[]>( queryString );

      String[] properties = query.remove( "property" );
      String[] sproperties = query.remove( "sproperty" );
      String[] algArray = query.remove( "alg" );
      String[] countArray = query.remove( "count" );
      String[] includeArray = query.remove( "include" );

      if ( sproperties == null ) {
        sproperties = new String[0];
      }

      if ( algArray == null || algArray.length == 0 ) {
        return TODO;
      }

      if ( countArray == null || countArray.length == 0 ) {
        return TODO;
      }

      boolean include = false;

      if ( includeArray != null && includeArray.length == 1 ) {
        include = Boolean.valueOf( includeArray[0] );
      }

      String alg = algArray[0];
      int count = Integer.valueOf( countArray[0] );

      Filter filter = Application.getFilterFromQuery( query );

      // TODO do this via controller
      PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
      RepresentativeGenerator samplesGen = new RepresentativeAlgorithmFactory().getAlgorithm( alg );
      Map<String, Object> samplesOptions = new HashMap<String, Object>();
      samplesOptions.put( "properties", Arrays.asList( sproperties ) );
      samplesGen.setOptions( samplesOptions );

      ProfileGenerator gen = new ProfileGenerator( persistence, samplesGen );

      // int hash = filter.hashCode();

      Document profile = gen.generateProfile( filter, count, include );
      String fileName = "profiles" + File.separator + "c3po-profile_" + filter.hashCode() + ".xml";
      gen.write( profile, fileName);

      File f = new File( fileName );
      if ( f.exists() ) {
        response().setContentType( "application/force-download" );
        response().setHeader( "Content-Transfer-Encoding", "binary" );
        response().setHeader( "Content-Disposition", "attachment; filename=\"" + fileName + "\"" );
        try {
          return ok( new FileInputStream( f ) );
        } catch ( FileNotFoundException e ) {
          e.printStackTrace();
          return internalServerError( e.getMessage() );
        }
      }

    } else {
      return badRequest( "The provided accept header is not supported: " + request().getHeader( "Accept" ) );
    }

    return ok();
  }
  //
  // public static Result profile() {
  // Logger.debug("Received a profile generation call");
  // final String accept = request().getHeader("Accept");
  //
  // final DynamicForm form = form().bindFromRequest();
  // final String c = form.get("collection");
  // final String e = form.get("includeelements");
  //
  // Filter filter = Application.getFilterFromSession();
  // boolean include = false;
  //
  // if (filter == null) {
  // if (c == null) {
  // return badRequest("No collection parameter provided\n");
  // } else if (!Application.getCollectionNames().contains(c)) {
  // return notFound("No collection with name " + c + " was found\n");
  // }
  //
  // filter = new Filter(c, null, null);
  // }
  //
  // if (e != null) {
  // include = Boolean.valueOf(e);
  // }
  //
  // if (accept.contains("*/*") || accept.contains("application/xml")) {
  // return profileAsXml(filter, include);
  // }
  //
  // Logger.debug("The accept header is not supported: " + accept);
  // return badRequest("The provided accept header '" + accept +
  // "' is not supported");
  // }
  //
  // public static Result exportAllToCSV() {
  // CSVGenerator generator = getGenerator();
  //
  // Filter filter = Application.getFilterFromSession();
  //
  // String collection = filter.getCollection();
  // String path = "exports/" + collection + "_" + filter.getDescriminator() +
  // "_matrix.csv";
  // generator.exportAll(collection, path);
  //
  // File file = new File(path);
  //
  // try {
  // return ok(new FileInputStream(file));
  // } catch (FileNotFoundException e) {
  // return internalServerError(e.getMessage());
  // }
  // }
  //
  // public static Result exportFilterToCSV() {
  // CSVGenerator generator = getGenerator();
  //
  // Filter filter = Application.getFilterFromSession();
  //
  // String collection = filter.getCollection();
  // String path = "exports/" + collection + "_" + filter.getDescriminator() +
  // "_matrix.csv";
  // generator.export(filter, path);
  //
  // File file = new File(path);
  //
  // try {
  // return ok(new FileInputStream(file));
  // } catch (FileNotFoundException e) {
  // return internalServerError(e.getMessage());
  // }
  // }
  //
  // private static CSVGenerator getGenerator() {
  // PersistenceLayer p =
  // Configurator.getDefaultConfigurator().getPersistence();
  // CSVGenerator generator = new CSVGenerator(p);
  //
  // return generator;
  //
  // }
  //
  // private static Result profileAsXml(Filter filter, boolean includeelements)
  // {
  // File result = generateProfile(filter, includeelements);
  //
  // return ok(result);
  // }
  //
  // private static File generateProfile(Filter filter, boolean includeelements)
  // {
  // StringBuilder pathBuilder = new StringBuilder();
  // pathBuilder.append("profiles/").append(filter.getCollection()).append("_").append(filter.getDescriminator());
  // if (includeelements) {
  // pathBuilder.append("_").append("elements");
  // }
  //
  // pathBuilder.append(".xml");
  //
  // String path = pathBuilder.toString();
  //
  // Logger.debug("Looking for collection profile " + path);
  //
  // File file = new File(path);
  //
  // if (!file.exists() || isCollectionUpdated(filter.getCollection()) ||
  // isNewFilter(file, filter)) {
  // Logger.debug("File does not exist. Generating profile for filter " +
  // filter.getDocument());
  // Configurator configurator = Configurator.getDefaultConfigurator();
  // PersistenceLayer p = configurator.getPersistence();
  // String alg = configurator.getStringProperty("c3po.samples.algorithm");
  // RepresentativeGenerator samplesGen = new
  // RepresentativeAlgorithmFactory().getAlgorithm(alg);
  // ProfileGenerator generator = new ProfileGenerator(p, samplesGen);
  // Document profile = generator.generateProfile(filter, includeelements);
  //
  // generator.write(profile, path);
  // file = new File(path);
  //
  // ActionLogHelper alHelper = new ActionLogHelper(p);
  // alHelper.recordAction(new ActionLog(filter.getCollection(),
  // ActionLog.PROFILE_ACTION));
  // }
  //
  // return file;
  // }
  //
  // private static boolean isCollectionUpdated(String collection) {
  // PersistenceLayer p =
  // Configurator.getDefaultConfigurator().getPersistence();
  // ActionLogHelper alHelper = new ActionLogHelper(p);
  // ActionLog lastAction = alHelper.getLastAction(collection);
  //
  // boolean isUpdated = true;
  //
  // if (lastAction != null) {
  // if (lastAction.getAction().equals(ActionLog.PROFILE_ACTION)) {
  // isUpdated = false;
  // }
  // }
  //
  // return isUpdated;
  // }
  //
  // private static boolean isNewFilter(File file, Filter filter) {
  // long profileFiltersCount = -1;
  // long profileObjectsCount = -1;
  //
  // boolean isNew = true;
  // try {
  // final SAXReader reader = new SAXReader();
  // Document doc = reader.read(file);
  // Element partition = doc.getRootElement().element("partition");
  // profileFiltersCount =
  // partition.element("filter").element("parameters").elements().size();
  // profileObjectsCount = Long.parseLong(partition.attributeValue("count"));
  //
  // } catch (final DocumentException e) {
  // //do nothing...
  // //just regenerate the profile...
  // }
  //
  // PersistenceLayer persistence =
  // Configurator.getDefaultConfigurator().getPersistence();
  // long filtersCount = persistence.count(Constants.TBL_FILTERS,
  // new BasicDBObject("descriminator", filter.getDescriminator()));
  // long filterObjectsCount = persistence.count(Constants.TBL_ELEMENTS,
  // Application.getFilterQuery(filter));
  //
  // if (filtersCount == profileFiltersCount && profileObjectsCount ==
  // filterObjectsCount) {
  // isNew = false;
  // }
  //
  // return isNew;
  //
  // }

}
