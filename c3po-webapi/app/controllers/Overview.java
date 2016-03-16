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

import helpers.Distribution;
import helpers.Graph;
import helpers.GraphData;
import helpers.PropertySetTemplate;
import helpers.Statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.overview;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;
import common.WebAppConstants;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.NumericStatistics;

public class Overview extends Controller {

	public static Result index() {
		Application.buildSession();
		Logger.debug("Received an index call in overview");
		final List<String> names = PropertyController.getCollectionNames();
		List<Graph> graphs = new ArrayList<Graph>();
		Distribution sizeDistribution=PropertyController.getDistribution("size");
		Filter filter=FilterController.getFilterFromSession();
		PropertySetTemplate.setProps(filter);
		for (String property: Application.PROPS){
			Distribution d= PropertyController.getDistribution(property);
			Graph g=new Graph(d.getProperty(), d.getPropertyValues(),d.getPropertyValueCounts());
			//
			g.cutLongTail();
			graphs.add( g );
		}
		GraphData data = new GraphData(graphs);

		Statistics stats = new Statistics();
		stats.setAvg(PropertyController.round(sizeDistribution.getStatistics().get("average")/1024/1024, 3) + " MB");
		stats.setCount( sizeDistribution.getStatistics().get("count").intValue() + " objects" );
		stats.setMax( PropertyController.round(sizeDistribution.getStatistics().get("max")/1024/1024, 3) + " MB" );
		stats.setMin( PropertyController.round(sizeDistribution.getStatistics().get("min")/1024/1024, 3) + " MB");
		stats.setSd( PropertyController.round(sizeDistribution.getStatistics().get("sd")/1024/1024, 3) + " MB");
		stats.setSize( PropertyController.round(sizeDistribution.getStatistics().get("sum")/1024/1024, 3) + " MB" );
		stats.setVar( PropertyController.round(sizeDistribution.getStatistics().get("var")/1024/1024, 3) + " MB");

		return ok(overview.render(names, data, stats, Templates.getCurrentTemplate()));
	}

	public static Result getGraph(String property) {
		Logger.debug("Received a getGraph call for property '" + property + "'");
		// if it is one of the default properties, do not draw..
		for (String p : Application.PROPS) {
			if (p.equals(property)) {
				return ok();
			}
		}

		Graph g = FilterController.getGraph(property);

		return ok(play.libs.Json.toJson(g));
	}

}
