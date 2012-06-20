package helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Filter {
  private String collection;
  private String filter;
  private String value;
  private List<String> values;
  private int batch;
  private int offset;

  public Filter() {
    this.values = new ArrayList<String>();
    this.batch = 25;
    this.offset = 0;
  }

  public Filter(String name, String filter) {
    this.collection = name;
    this.filter = filter;
    this.values = new ArrayList<String>();
    this.batch = 25;
    this.offset = 0;
  }

  public String getCollection() {
    return collection;
  }

  public void setCollection(String name) {
    this.collection = name;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public int getBatch() {
    return batch;
  }

  public void setBatch(int batch) {
    this.batch = batch;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String toString() {
    return "filter: " + filter + " name: " + collection;
  }

  public void sortValues(Map<String, String> values) {
    this.values = new ArrayList<String>(values.keySet());
    Collections.sort(this.values, new HistogrammSorter(values));
  }

  private class HistogrammSorter implements Comparator<String> {
    private Map<String, String> values;

    public HistogrammSorter(Map<String, String> values) {
      this.values = values;
    }

    @Override
    public int compare(String s1, String s2) {
      return Double.valueOf(values.get(s2)).compareTo(Double.valueOf(values.get(s1)));
    }
  }

}
