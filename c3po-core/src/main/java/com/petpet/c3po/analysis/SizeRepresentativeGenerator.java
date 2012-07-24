package com.petpet.c3po.analysis;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;

public class SizeRepresentativeGenerator extends RepresentativeGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(SizeRepresentativeGenerator.class);

  private PersistenceLayer pl;

  public SizeRepresentativeGenerator() {
    this.pl = Configurator.getDefaultConfigurator().getPersistence();
  }

  @Override
  public List<String> execute() {
    return execute(10);
  }

  @Override
  public List<String> execute(int limit) {
    final List<String> result = new ArrayList<String>();
    final BasicDBObject query = DataHelper.getFilterQuery(this.getFilter());
    LOG.debug("Query: " + query.toString());
    long count = pl.count(Constants.TBL_ELEMENTS, query);
    if (count <= limit) {
      DBCursor cursor = this.pl.find(Constants.TBL_ELEMENTS, query);
      while (cursor.hasNext()) {
        result.add(DataHelper.parseElement(cursor.next(), this.pl).getUid());
      }
    } else {
      final NumericAggregationJob job = new NumericAggregationJob(this.getFilter().getCollection(), "size");
      job.setFilterquery(query);

      final MapReduceOutput output = job.execute();
      final List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
      final BasicDBObject aggregation = (BasicDBObject) results.get(0).get("value");

      double min = aggregation.getDouble("min");
      double max = aggregation.getDouble("max");
      double avg = aggregation.getDouble("avg");
      double sd = aggregation.getDouble("stddev");
      double low = Math.floor((avg - sd / 10));
      double high = Math.ceil((avg + sd / 10));

      // System.out.println("min " + min);
      // System.out.println("max " + max);
      // System.out.println("avg " + avg);
      // System.out.println("sd " + sd);
      // System.out.println("low " + low);
      // System.out.println("high " + high);

      final BasicDBObject minQuery = new BasicDBObject(query).append("metadata.size.value", min);
      final BasicDBObject maxQuery = new BasicDBObject(query).append("metadata.size.value", max);
      List<BasicDBObject> and = new ArrayList<BasicDBObject>();
      and.add(new BasicDBObject("metadata.size.value", new BasicDBObject("$lte", high)));
      and.add(new BasicDBObject("metadata.size.value", new BasicDBObject("$gte", low)));
      final BasicDBObject avgQuery = new BasicDBObject(query).append("$and", and);

      final DBCursor minCursor = this.pl.find(Constants.TBL_ELEMENTS, minQuery);
      final DBCursor maxCursor = this.pl.find(Constants.TBL_ELEMENTS, maxQuery);
      final DBCursor avgCursor = this.pl.find(Constants.TBL_ELEMENTS, avgQuery);

      if (minCursor.count() > 0) {
        result.add(DataHelper.parseElement(minCursor.next(), this.pl).getUid());
      }

      if (maxCursor.count() > 0) {
        result.add(DataHelper.parseElement(maxCursor.next(), this.pl).getUid());
      }

      if (avgCursor.count() >= (limit - 2)) {
        for (int i = 0; i < limit - 2; i++) {
          result.add(DataHelper.parseElement(avgCursor.next(), this.pl).getUid());
        }
      } else {
        while (avgCursor.hasNext()) {
          result.add(DataHelper.parseElement(avgCursor.next(), this.pl).getUid());
        }
      }

    }

    return result;
  }
  
  public String getType() {
    return "size'o'matic 3000";
  }
}
