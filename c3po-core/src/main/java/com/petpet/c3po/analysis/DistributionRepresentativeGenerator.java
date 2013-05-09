package com.petpet.c3po.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.utils.Configurator;

public class DistributionRepresentativeGenerator extends RepresentativeGenerator {

  @Override
  public List<String> execute() {
    return this.execute(10);
  }

  @Override
  public List<String> execute(int limit) {
    final List<String> properties = this.getProperties();
    final List<String> result = new ArrayList<String>();

    if (properties.isEmpty()) {
      throw new IllegalArgumentException("No properties were provided for distribution calculation");
    }

    // for each property
    // find all distinct property value pairs
    // e.g. for valid and well-formed:
    // valid=yes, well-formed=yes; valid=yes, well-formed=no; valid=no,
    // well-formed=no; valid=no, well-formed=yes;
    // valid=unknown, well-formed=yes; valid=unknown, well-formed=no; valid=yes,
    // well-formed=unknown; valid=no, well-formed=unknown;
    // valid=unknown, well-formed=unknown

    // for each distinct property value pair (combination) find the file
    // occurrences and sort the property value pair
    // (combinations) descending.

    // start from the most occurring one and calculate its percentage of the
    // overall count (consider the filter query)

    // based on the percentage, calculate the absolute value (round up) and
    // query 'n' random elements from the filter set
    // that have this property-value pair (combination).

    // proceed with the last 2 steps until the limit is reached.

    PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    long overallCount = pl.count(Element.class, this.getFilter());
    FilterCondition[][] matrix = new FilterCondition[properties.size()][];
    for (int i = 0; i < properties.size(); i++) {
      String key = properties.get(i);
      List distinct = pl.distinct(Element.class, properties.get(i), this.getFilter());
      FilterCondition[] values = new FilterCondition[distinct.size()];
      for (int j = 0; j < distinct.size(); j++) {
        values[j] = new FilterCondition(key, distinct.get(j));
      }

      matrix[i] = values;

    }

//    System.out.println(Arrays.deepToString(matrix));

    List<Combination> combinations = new ArrayList<Combination>();
    Set<List<FilterCondition>> results = this.combinations(matrix);
    for (List<FilterCondition> combs : results) {
      Filter query = new Filter(this.getFilter());
      for (FilterCondition c : combs) {
        query.addFilterCondition(c);
      }

      //TODO change query to a new query...
      long count = pl.count(Element.class, query);
      // System.out.println(query.toString() + " " + count);
      combinations.add(new Combination(query, count));
    }

    Collections.sort(combinations, new CombinationComparator());

    for (Combination c : combinations) {
      if (c.count > 0 && result.size() < limit) {
        double percent = c.count * 100 / overallCount;
        int tmpLimit = (int) Math.round(percent / 100 * limit);

        Iterator<Element> cursor = pl.find(Element.class, c.query);
//        System.out.println(c.query + " count: " + c.count + " percent: " + percent + "% absolute: " + tmpLimit);
        while (cursor.hasNext() && tmpLimit != 0 && result.size() < limit) {
          result.add(cursor.next().getUid());
          tmpLimit--;
        }
      }

    }

    return result;
  }

  private Set<List<FilterCondition>> combinations(FilterCondition[][] opts) {

    Set<List<FilterCondition>> results = new HashSet<List<FilterCondition>>();

    if (opts.length == 1) {
      for (FilterCondition s : opts[0])
        results.add(new ArrayList<FilterCondition>(Arrays.asList(s)));
    } else
      for (FilterCondition obj : opts[0]) {
        FilterCondition[][] tail = Arrays.copyOfRange(opts, 1, opts.length);
        for (List<FilterCondition> combs : combinations(tail)) {
          combs.add(obj);
          results.add(combs);
        }
      }
    return results;
  }

  private static class Combination {

    private Filter query;
    private long count;

    public Combination(Filter query, long count) {
      this.query = query;
      this.count = count;
    }

  }

  private class CombinationComparator implements Comparator<Combination> {
    @Override
    public int compare(Combination c1, Combination c2) {
      return new Long(c2.count).compareTo(c1.count); // descending
    }

  }

  @Override
  public String getType() {
    return "distribution sampling";
  }

  private List<String> getProperties() {
    final Map<String, Object> options = this.getOptions();
    List<String> properties = (List<String>) options.get("properties");

    if (properties == null) {
      properties = new ArrayList<String>();
    }

    return properties;
  }

//  public static void main(String... args) {
//    Configurator configurator = Configurator.getDefaultConfigurator();
//    configurator.configure();
//   // PersistenceLayer pl = configurator.getPersistence();
//
//    Map<String, Object> options = new HashMap<String, Object>();
//    options.put("properties", Arrays.asList("valid", "format"));
//
//    Filter f = new Filter(new FilterCondition("collection", "test"));
//    DistributionRepresentativeGenerator drg = new DistributionRepresentativeGenerator();
//    drg.setOptions(options);
//    drg.setFilter(f);
//
//    List<String> result = drg.execute();
//    System.out.println("REPRESENTATIVES");
//    for (String s : result) {
//      System.out.println(s);
//    }
//  }

}
