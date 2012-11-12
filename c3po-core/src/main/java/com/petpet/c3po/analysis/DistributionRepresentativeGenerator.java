package com.petpet.c3po.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;

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

    BasicDBObject filterQuery = DataHelper.getFilterQuery(this.getFilter());
    PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    long overallCount = pl.count(Constants.TBL_ELEMENTS, filterQuery);
    BasicDBObject[][] matrix = new BasicDBObject[properties.size()][];
    for (int i = 0; i < properties.size(); i++) {
      String key = "metadata." + properties.get(i) + ".value";
      List distinct = pl.distinct(Constants.TBL_ELEMENTS, key, filterQuery);
      BasicDBObject[] values = new BasicDBObject[distinct.size()];
      for (int j = 0; j < distinct.size(); j++) {
        // long count = pl.count(Constants.TBL_ELEMENTS, filterQuery.append(key,
        // o));
        // System.out.println(key + ": " + o + " - " + count);
        values[j] = new BasicDBObject(key, distinct.get(j));
      }

      matrix[i] = values;

      // MapReduceJob job = new HistogramJob(this.getFilter().getCollection(),
      // prop, filterQuery);
      // MapReduceOutput execute = job.execute();
      //
      // List<BasicDBObject> results = (List<BasicDBObject>)
      // execute.getCommandResult().get("results");
      // System.out.println(execute);
    }

    System.out.println(Arrays.deepToString(matrix));

    List<Combination> combinations = new ArrayList<Combination>();
    Set<List<BasicDBObject>> results = this.combinations(matrix);
    for (List<BasicDBObject> combs : results) {
      BasicDBObject query = new BasicDBObject(filterQuery.toMap());
      for (BasicDBObject c : combs) {
        query.putAll(c.toMap());
      }

      long count = pl.count(Constants.TBL_ELEMENTS, query);
      // System.out.println(query.toString() + " " + count);
      combinations.add(new Combination(query, count));
    }

    Collections.sort(combinations, new CombinationComparator());

    for (Combination c : combinations) {
      if (c.count > 0 && result.size() < limit) {
        double percent = c.count * 100 / overallCount;
        int tmpLimit = (int) Math.round(percent / 100 * limit);

        DBCursor cursor = pl.find(Constants.TBL_ELEMENTS, c.query).limit(tmpLimit);
        while (cursor.hasNext() && result.size() < limit) {
          String uid = DataHelper.parseElement(cursor.next(), pl).getUid();
          result.add(uid);
        }
        System.out.println(c.query + " count: " + c.count + " percent: " + percent + "% absolute: " + tmpLimit);
      }

    }

    return result;
  }

  private Set<List<BasicDBObject>> combinations(BasicDBObject[][] opts) {

    Set<List<BasicDBObject>> results = new HashSet<List<BasicDBObject>>();

    if (opts.length == 1) {
      for (BasicDBObject s : opts[0])
        results.add(new ArrayList<BasicDBObject>(Arrays.asList(s)));
    } else
      for (BasicDBObject obj : opts[0]) {
        BasicDBObject[][] tail = Arrays.copyOfRange(opts, 1, opts.length);
        for (List<BasicDBObject> combs : combinations(tail)) {
          combs.add(obj);
          results.add(combs);
        }
      }
    return results;
  }

  private static class Combination {

    private DBObject query;
    private long count;

    public Combination(DBObject query, long count) {
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
    // unchecked ...
    List<String> properties = (List<String>) options.get("properties");

    if (properties == null) {
      properties = new ArrayList<String>();
    }

    return properties;
  }

  public static void main(String... args) {
    Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();
    PersistenceLayer pl = configurator.getPersistence();

    Map<String, Object> options = new HashMap<String, Object>();
    options.put("properties", Arrays.asList("valid", "format"));

    Filter f = new Filter("roda", null, null);
    DistributionRepresentativeGenerator drg = new DistributionRepresentativeGenerator();
    drg.setOptions(options);
    drg.setFilter(f);

    List<String> result = drg.execute();
    System.out.println("REPRESENTATIVES");
    for (String s : result) {
      System.out.println(s);
    }
  }

}
