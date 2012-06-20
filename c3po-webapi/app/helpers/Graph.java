package helpers;

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
}
