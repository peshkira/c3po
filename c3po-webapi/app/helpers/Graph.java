package helpers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Graph {

  private String property;
  private List<String> keys;
  private List<String> values;

  public Graph() {

  }

  public Graph(String p, List<String> keys, List<String> values) {
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
      k.add(keys.remove(pos));
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
}
