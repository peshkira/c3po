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
import java.util.List;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.samples;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.analysis.RepresentativeAlgorithmFactory;
import com.petpet.c3po.analysis.RepresentativeGenerator;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;

public class Samples extends Controller {

  public static Result index() {
    final List<String> names = Application.getCollectionNames();
    return ok(samples.render(names));
  }

  public static Result getSamples(String alg, int size, String props) {
    Logger.debug("in method getSamples alg " + alg + " size " + size + " props " + props);
    final Configurator configurator = Configurator.getDefaultConfigurator();
    final PersistenceLayer pl = configurator.getPersistence();
    final Filter filter = Application.getFilterFromSession();
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
      BasicDBObject query = new BasicDBObject("uid", new BasicDBObject("$in", output));
      DBCursor cursor = pl.find(Constants.TBL_ELEMENTS, query);
      while (cursor.hasNext()) {
        samples.add(DataHelper.parseElement(cursor.next(), pl));
      }
    }

    return ok(play.libs.Json.toJson(samples));
  }

}
