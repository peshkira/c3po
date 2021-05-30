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

import helpers.TemplatesLoader;
import play.Application;
import play.GlobalSettings;
import play.Logger;

//import com.mongodb.DBCollection;
//import com.mongodb.MapReduceCommand.OutputType;
//import com.petpet.c3po.analysis.mapreduce.HistogramJob;
//import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.utils.Configurator;

public class Global extends GlobalSettings {

  @Override
  public void onStart(Application app) {
    Logger.info("Starting c3po web app");
    super.onStart(app);
    Configurator.getDefaultConfigurator().configure();

    TemplatesLoader.updateConfig();
  }
  // TODO think of a better way to decide when to drop the
  // mapreduce results.
  @Override
  public void onStop(Application app) {
    Logger.info("Stopping c3po web app");
    super.onStop(app);
  }
}
