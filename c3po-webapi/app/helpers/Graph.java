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
package helpers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.utils.Configurator;

import controllers.FilterController;
import controllers.PropertyController;
import play.Logger;
import play.data.DynamicForm;


public class Graph {

  private String property;
  private Map<String, String> options;
  private List<String> keys;
  private List<String> values;

  public Graph() {
    this.setOptions(new HashMap<String, String>());
  }

  public Graph(String p, List<String> keys, List<String> values) {
    this();
    this.property = p;
    this.keys = keys;
    this.values = values;
  }

  public List<String> getKeys() {
    return keys;
  }

  public void setKeys(List<String> keys) {
    this.keys = keys;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public void convertToPercentage() {
    double sum = 0;

    for (String s : values) {
      sum += Double.parseDouble(s);
    }

    List<String> res = new ArrayList<String>();
    for (String s : values) {
      final DecimalFormat df = new DecimalFormat("#.##");
      double d = ((Double.parseDouble(s) / sum) * 100);
      res.add(df.format(d) + "");
    }

    this.values = res;
  }

  public void sort() {
    List<String> k = new ArrayList<String>();
    List<String> v = new ArrayList<String>();
    int target = values.size();
    while (v.size() != target) {
      double max = -1;
      int pos = 0;
      for (int i = 0; i < values.size(); i++) {
        double current = Double.parseDouble(values.get(i));
        if (current > max) {
          max = current;
          pos = i;
        }
      }
      String val = values.remove(pos);
        
      if (val.endsWith(".0")) {
        val = val.substring(0, val.length() - 2);
      }
      
      v.add(val);
      String key="";
      try{
    	  key=keys.remove(pos);
    	  k.add(key);
      } catch(IndexOutOfBoundsException e)
      {
    	  Logger.warn("Index " + key + " is out of bounds of the list when created a graph" );
      }
      
    }

    this.keys = k;
    this.values = v;
  }
  
  public void cutLongTail() {
    List<String> k = new ArrayList<String>();
    List<String> v = new ArrayList<String>();
    
    double sum = 0D;
    double rest = 0D;
    
    for (String s : values) {
      sum += Double.parseDouble(s);
    }
    
    int cut = (int)(sum * 0.005);
    
    for (int i = 0; i < values.size(); i++) {
      double tmp = Double.parseDouble(values.get(i));
      if (tmp > cut) {
        k.add(keys.get(i));
        v.add(values.get(i));
      } else {
        rest += tmp;
      }
    }
    
    if (rest > 0) {
    k.add("Rest");
    v.add("" + rest);
    }
    
    this.keys = k;
    this.values = v;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }

/*public static Graph getFixedWidthHistogram(Filter filter, String property, int width) {
	//BasicDBObject query = FilterController.getFilterQuery(filter);

	final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();

	Property p = pl.getCache().getProperty( property );
	Map<String, Long> hist = pl.getValueHistogramFor(p , filter );
	Graph g = null;
	return g;

}*/

public static Graph getGraph(Filter filter, String property) {
    DynamicForm form = play.data.Form.form().bindFromRequest();
    String alg = form.get("alg");
    String width = form.get("width");
    if (width.equals("-1"))
        width=null;
   // Distribution d = PropertyController.getDistribution(property, filter, alg, width);
	Distribution d=PropertyController.getDistribution(property, filter, alg, width );
	Graph g = new Graph( d.getProperty(), d.getPropertyValues(), d.getPropertyValueCounts() );
	return g;
}


/*
public static Graph getNumericGraph(Filter filter, String property, String alg, String w) {

	// TODO find number of elements based on filter...
	// calculate bins...
	// find classes based on number of bins...
	// map reduce this property based on the classes...
	Graph g = null;
	if (alg.equals("sturge")) {
		// bins = log2 n + 1
		g = getSturgesHistogramm(filter, property);
	} else if (alg.equals("sqrt")) {
		// bins = sqrt(n);
		g = getSquareRootHistogram(filter, property);
	} else {
		alg="fixed";
		int width = 50;
		try {
			width = Integer.parseInt(w);
		} catch (NumberFormatException e) {
			Logger.warn("Not a number, using default bin width: 50");
		}

		g = getFixedWidthHistogram(filter, property, width);
		g.getOptions().put("width", w);


	}

	g.getOptions().put("type", PropertyType.INTEGER.toString());
	g.getOptions().put("alg", alg);


	g.sort();

	return g;
}*/
/*
public static Graph getOrdinalGraph(Filter filter, String property) {
	Graph g = null;
	if (filter != null) {
		final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();

		Property p = pl.getCache().getProperty( property );
		Map<String, Long> hist = pl.getValueHistogramFor(p , filter );
		g = Graph.getGraph(filter, property);


	}

	return g;
}*/
/*

public static Graph getSquareRootHistogram(Filter f, String property) {
	//BasicDBObject query = FilterController.getFilterQuery(f);
	//DBCursor cursor = Configurator.getDefaultConfigurator().getPersistence().find(Constants.TBL_ELEMENTS, query);
	//int n = cursor.size();
	//int bins = (int) Math.sqrt(n);
	//MapReduceJob job = new NumericAggregationJob(f.getCollection(), property);
	//job.setFilterquery(query);

	// MapReduceOutput output = job.execute();
	// List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
	Graph g = null;
	*/
/*if (!results.isEmpty()) {
  BasicDBObject aggregation = (BasicDBObject) results.get(0).get("value");
  long max = aggregation.getLong("max");
  int width = (int) (max / bins);
  Map<String, String> config = new HashMap<String, String>();
  config.put("bin_width", width + "");

  job = new HistogramJob(f.getCollection(), property);
  job.setFilterquery(query);
  job.setConfig(config);
  output = job.execute();
  List<String> keys = new ArrayList<String>();
  List<String> values = new ArrayList<String>();

  calculateNumericHistogramResults(output, keys, values, width);

  g = new Graph(property, keys, values);
}*//*


	return g;
}

public static Graph getSturgesHistogramm(Filter f, String property) {
	//BasicDBObject query = FilterController.getFilterQuery(f);
	//DBCursor cursor = Configurator.getDefaultConfigurator().getPersistence().find(Constants.TBL_ELEMENTS, query);
	//int n = cursor.size();
	//int bins = (int) ((Math.log(n) / Math.log(2)) + 1);
	//MapReduceJob job = new NumericAggregationJob(f.getCollection(), property);
	//job.setFilterquery(query);

	//MapReduceOutput output = job.execute();
	//List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
	Graph g = null;
	*/
/* if (!results.isEmpty()) {
  BasicDBObject aggregation = (BasicDBObject) results.get(0).get("value");
  long max = aggregation.getLong("max");
  int width = (int) (max / bins);
  Map<String, String> config = new HashMap<String, String>();
  config.put("bin_width", width + "");

  job = new HistogramJob(f.getCollection(), property);
  job.setFilterquery(query);
  job.setConfig(config);
  output = job.execute();
  List<String> keys = new ArrayList<String>();
  List<String> values = new ArrayList<String>();

  calculateNumericHistogramResults(output, keys, values, width);

  g = new Graph(property, keys, values);
}*//*


	return g;
}
*/

}
