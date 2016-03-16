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
import java.util.List;

import play.Application;
import play.GlobalSettings;
import play.Logger;

//import com.mongodb.DBCollection;
//import com.mongodb.MapReduceCommand.OutputType;
//import com.petpet.c3po.analysis.mapreduce.HistogramJob;
//import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.utils.Configurator;

import helpers.PropertySetTemplate;

public class Global extends GlobalSettings {

  @Override
  public void onStart(Application app) {
    Logger.info("Starting c3po web app");
    super.onStart(app);
    Configurator.getDefaultConfigurator().configure();
    
 //   this.calculateCollectionStatistics();
 //   this.calculateHistogramms();
    PropertySetTemplate.updateConfig();
  }

  // TODO think of a better way to decide when to drop the
  // mapreduce results.
  @Override
  public void onStop(Application app) {
    Logger.info("Stopping c3po web app");
    super.onStop(app);
  }
  
//  @Override
//  public Result onBadRequest(String uri, String error){
//    Logger.error("Bad Request: " + uri + " " + error);
//    return badRequest(uri, error);
//  }
//  
//  public Result onHandlerNotFound(String uri) {
//    Logger.error("Handler not found: " + uri);
//    return notFound();
//  }

  private void calculateCollectionStatistics() {
    Logger.info("Calculating size statistics of each collection");
    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    final List<String> collections = controllers.PropertyController.getCollectionNames();
    Property size = pl.getCache().getProperty( "size" );
    for (String collection : collections) {
      final String cName = "statistics_" + collection;
      Filter filter=new Filter();
      filter.addFilterCondition(new FilterCondition("collection", collection));
      NumericStatistics statistics = pl.getNumericStatistics( size, filter );
      //final DBCollection c = pl.getDB().getCollection(cName);

      //if (c.find().count() == 0) {
      ///  Logger.info("No statistics found for collection " + collection + ", rebuilding");

      //  final NumericAggregationJob job = new NumericAggregationJob(collection, "size");
      //  job.setType(OutputType.REPLACE);
      //  job.setOutputCollection(cName);
      //  job.execute();
     // }
    }
  }

  private void calculateHistogramms() {
    Logger.info("Calculating histograms of each collection");
    final List<String> collections = controllers.PropertyController.getCollectionNames();
    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();

    for (String collection : collections) {
      for (String p : controllers.Application.PROPS) {
        final String cName = "histogram_" + collection + "_" + p;
        Property prop = pl.getCache().getProperty( p );
        
        Filter filter=new Filter();
        filter.addFilterCondition(new FilterCondition("collection", collection));
         pl.getValueHistogramFor( prop, filter );
        
        
        //final DBCollection c = pl.getDB().getCollection(cName);

       // if (c.find().count() == 0) {
       //   Logger.info("No histogram found for collection " + collection + "  and property " + p + ", rebuilding");

        //  final HistogramJob job = new HistogramJob(collection, p);
        //  job.setType(OutputType.REPLACE);
        //  job.setOutputCollection(cName);
        //  job.execute();
        //}
      }
    }
  }

}
