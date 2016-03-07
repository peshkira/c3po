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

import helpers.Graph;
import helpers.GraphData;
import helpers.Statistics;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.overview;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;
import common.WebAppConstants;

public class Overview extends Controller {

  public static Result index() {
    final List<String> names = Application.getCollectionNames();
    Filter filter = Application.getFilterFromSession();

    Statistics stats = null;
    GraphData data = null;
    if (filter != null) {
      BasicDBObject ref = new BasicDBObject("descriminator", filter.getDescriminator());
      ref.put("collection", session(WebAppConstants.CURRENT_COLLECTION_SESSION));
      DBCursor cursor = Configurator.getDefaultConfigurator().getPersistence().find(Constants.TBL_FILTERS, ref);

      Logger.info("filter is not null");
      if (cursor.count() == 1) { // only root filter
        Logger.info("filter has no parent, using cached statistics");
        // used cached results
        stats = FilterController.getCollectionStatistics(filter.getCollection());
        data = Overview.getDefaultGraphs(filter, true);
      } else {
        // calculate new results
        stats = FilterController.getCollectionStatistics(filter);

        List<String> properties = new ArrayList<String>();
        while (cursor.hasNext()) {
          Filter tmp = DataHelper.parseFilter(cursor.next());
          if (tmp.getProperty() != null) {
            properties.add(tmp.getProperty());
          }
        }
        data = Overview.getAllGraphs(filter, properties);
      }

    }
    return ok(overview.render(names, data, stats, Templates.getCurrentTemplate()));
  }

  public static Result getGraph(String property) {

    // if it is one of the default properties, do not draw..
    for (String p : Application.PROPS) {
      if (p.equals(property)) {
        return ok();
      }
    }

    Graph g = FilterController.getGraph(property);

    return ok(play.libs.Json.toJson(g));
  }

  private static GraphData getDefaultGraphs(Filter f, boolean root) {
    List<Graph> graphs = new ArrayList<Graph>();
    for (String prop : Application.PROPS) {
      Graph graph;
      if (root) {
        graph = FilterController.getGraph(f.getCollection(), prop);
      } else {
        graph = FilterController.getGraph(f, prop);
      }

      graphs.add(graph);

      // TODO decide when to cut long tail...
    }

    return new GraphData(graphs);
  }

  private static GraphData getAllGraphs(Filter f, List<String> props) {
    GraphData graphs = getDefaultGraphs(f, false);

    for (String prop : props) {
      boolean found = false;
      for (String def : Application.PROPS) {
        if (prop.equals(def)) {
          found = true;
          break;
        }
      }

      if (!found) {
        Graph graph = FilterController.getGraph(f, prop);
        graphs.getGraphs().add(graph);
      }
    }

    return graphs;
  }

}
