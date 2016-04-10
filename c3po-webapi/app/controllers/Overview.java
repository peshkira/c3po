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

import helpers.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.overview;

import com.petpet.c3po.api.model.helper.Filter;

public class Overview extends Controller {
    public static GraphData getAllGraphs() {
        return allGraphs;
    }

    static GraphData allGraphs=new GraphData();
    public static Result index() {
        Application.buildSession();
        Logger.debug("Received an index call in overview");
        final List<String> names = Properties.getCollectionNames();
        List<Graph> graphs = new ArrayList<Graph>();


        Filter filter = Filters.getFilterFromSession();
        TemplatesLoader.setProps(filter);
        for (String property : Application.PROPS) {
            Distribution d = Properties.getDistribution(property, filter);
            Graph g = Properties.interpretDistribution(d, null, null);
            g.cutLongTail();
            graphs.add(g);
            //addToAllGraphs(g);


        }
       // if (allGraphs.getGraphs().size()==0)
        allGraphs = new GraphData(graphs);
        Map<String, Double> statistics = Properties.getStatistics("size");
        StatisticsToPrint stats = new StatisticsToPrint();
        stats.setAvg(Properties.round(statistics.get("average") / 1024.0 / 1024.0, 3) + " MB");
        stats.setCount(statistics.get("count").intValue() + " objects");
        stats.setMax(Properties.round(statistics.get("max") / 1024.0 / 1024.0, 3) + " MB");
        stats.setMin(Properties.round(statistics.get("min") / 1024.0 / 1024.0, 3) + " MB");
        stats.setSd(Properties.round(statistics.get("sd") / 1024.0 / 1024.0, 3) + " MB");
        stats.setSize(Properties.round(statistics.get("sum") / 1024.0 / 1024.0, 3) + " MB");
        stats.setVar(Properties.round(statistics.get("var") / 1024.0 / 1024.0, 3) + " MB^2");

        return ok(overview.render(names, allGraphs, stats, TemplatesLoader.getCurrentTemplate()));
    }

    public static void addToAllGraphs(Graph g){
        List<Graph> graphs = allGraphs.getGraphs();
        Iterator<Graph> iterator = graphs.iterator();
        while (iterator.hasNext()){
            Graph next = iterator.next();
            if (next.getProperty().equals(g.getProperty())){
                if (next.getFilter()!=null && g.getFilter()!=null && !next.getFilter().equals(g.getFilter())){
                    next=g;
                    return;
                }
            }
        }
        graphs.add(g);
    }

    public static Result addGraph(String property) {
        Logger.debug("Received a addGraph call for property '" + property + "'");

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
        Filter filter = Filters.getFilterFromSession();
        Distribution d = Properties.getDistribution(property, filter);
        Graph g = Properties.interpretDistribution(d,alg,width);
        g.cutLongTail();
        allGraphs.getGraphs().add(g);
        TemplatesLoader.addUserDefinedGraph(property);
        return ok(play.libs.Json.toJson(g));
    }

}
