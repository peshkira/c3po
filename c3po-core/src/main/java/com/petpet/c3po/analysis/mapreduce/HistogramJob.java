package com.petpet.c3po.analysis.mapreduce;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.Property.PropertyType;

public class HistogramJob extends MapReduceJob {

  private static final Logger LOG = LoggerFactory.getLogger(HistogramJob.class);

  private String property;

  public HistogramJob(String c, String f) {
    this.setC3poCollection(c);
    this.property = f;
    this.setFilterquery(new BasicDBObject("collection", c));
  }

  public HistogramJob(String c, String f, BasicDBObject query) {
    this(c, f);
    this.setFilterquery(query);
  }

  public MapReduceOutput execute() {
    final Property p = this.getPersistence().getCache().getProperty(property);
    String map;
    if (p.getType().equals(PropertyType.DATE.toString())) {
      map = Constants.DATE_HISTOGRAM_MAP.replace("{}", p.getId());

      String constrainKey = "metadata." + this.property + ".value";
      BasicDBObject constraintValue = new BasicDBObject("$type", 9);
      BasicDBObject prop = (BasicDBObject) this.getFilterquery().remove("metadata." + this.property + ".value");
      if (prop != null) {
        LOG.info("Old Date Property: " + prop.toString());
        List<BasicDBObject> and = new ArrayList<BasicDBObject>();
        and.add(new BasicDBObject("metadata." + this.property + ".value", prop));
        and.add(new BasicDBObject(constrainKey, constraintValue));
        this.getFilterquery().put("$and", and);// for date...
      } else {
        this.getFilterquery().append(constrainKey, constraintValue);
      }

      LOG.info("Date Filter Query adjusted: " + this.getFilterquery().toString());
    } else if (p.getType().equals(PropertyType.INTEGER.toString()) || p.getType().equals(PropertyType.FLOAT.toString())) {
      String width = this.getConfig().get("bin_width");
      map = Constants.NUMERIC_HISTOGRAM_MAP.replace("{1}", this.property).replace("{2}", width);
      
    } else {
      map = Constants.HISTOGRAM_MAP.replace("{}", p.getId());
    }

    LOG.info("Executing histogramm map reduce job with following map:\n{}", map);
    final DBCollection elements = this.getPersistence().getDB().getCollection(Constants.TBL_ELEMENTS);
    final MapReduceCommand cmd = new MapReduceCommand(elements, map, Constants.HISTOGRAM_REDUCE,
        this.getOutputCollection(), this.getType(), this.getFilterquery());

    return this.getPersistence().mapreduce(Constants.TBL_ELEMENTS, cmd);
  }
}
