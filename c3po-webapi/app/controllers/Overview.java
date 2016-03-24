/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package controllers;

import helpers.Distribution;
import helpers.Graph;
import helpers.GraphData;
import helpers.StatisticsToPrint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.overview;

import com.petpet.c3po.api.model.helper.Filter;

public class Overview extends Controller {
    public static GraphData allGraphs=new GraphData();
    public static Result index() {
        Application.buildSession();
        Logger.debug("Received an index call in overview");
        final List<String> names = PropertyController.getCollectionNames();
        List<Graph> graphs = new ArrayList<Graph>();


        Filter filter = FilterController.getFilterFromSession();
        Templates.setProps(filter);
        for (String property : Application.PROPS) {
            Distribution d = PropertyController.getDistribution(property, filter, null, null);
            Graph g = new Graph(d.getProperty(), d.getPropertyValues(), d.getPropertyValueCounts());
            g.cutLongTail();
            graphs.add(g);
        }
        allGraphs = new GraphData(graphs);
        Map<String, Double> statistics = PropertyController.getStatistics("size");
        StatisticsToPrint stats = new StatisticsToPrint();
        stats.setAvg(PropertyController.round(statistics.get("average") / 1024.0 / 1024.0, 3) + " MB");
        stats.setCount(statistics.get("count").intValue() + " objects");
        stats.setMax(PropertyController.round(statistics.get("max") / 1024.0 / 1024.0, 3) + " MB");
        stats.setMin(PropertyController.round(statistics.get("min") / 1024.0 / 1024.0, 3) + " MB");
        stats.setSd(PropertyController.round(statistics.get("sd") / 1024.0 / 1024.0, 3) + " MB");
        stats.setSize(PropertyController.round(statistics.get("sum") / 1024.0 / 1024.0, 3) + " MB");
        stats.setVar(PropertyController.round(statistics.get("var") / 1024.0 / 1024.0, 3) + " MB^2");

        return ok(overview.render(names, allGraphs, stats, Templates.getCurrentTemplate()));
    }

    public static Result getGraph(String property) {
        Logger.debug("Received a getGraph call for property '" + property + "'");

        // if it is one of the default properties, do not draw..
        for (String p : Application.PROPS) {
            if (p.equals(property)) {
                return ok();
            }
        }
        DynamicForm form = play.data.Form.form().bindFromRequest();
        String alg = form.get("alg");
        String width = form.get("width");
        if (width!=null && width.equals("-1"))
            width=null;

        Filter filter = FilterController.getFilterFromSession();
        Distribution d = PropertyController.getDistribution(property, filter, alg, width);
        Graph g = new Graph(d.getProperty(), d.getPropertyValues(), d.getPropertyValueCounts());
        g.cutLongTail();
        allGraphs.getGraphs().add(g);

        return ok(play.libs.Json.toJson(g));
    }

}
