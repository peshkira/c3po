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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;


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

}
