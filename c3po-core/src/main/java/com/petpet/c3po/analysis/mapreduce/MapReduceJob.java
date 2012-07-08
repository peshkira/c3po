package com.petpet.c3po.analysis.mapreduce;

import com.mongodb.BasicDBObject;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.utils.Configurator;

public abstract class MapReduceJob {
  
  private PersistenceLayer persistence;
  
  private String outputCollection;
  
  private OutputType type;
  
  private String c3poCollection;
  
  private BasicDBObject filterquery;

  public PersistenceLayer getPersistence() {
    if (this.persistence == null) {
      this.persistence = Configurator.getDefaultConfigurator().getPersistence();
    }
    return persistence;
  }

  public void setPersistence(PersistenceLayer persistence) {
    this.persistence = persistence;
  }

  public String getOutputCollection() {
    return outputCollection;
  }

  public void setOutputCollection(String outputCollection) {
    this.outputCollection = outputCollection;
  }

  public OutputType getType() {
    if (this.type == null) {
      this.type = OutputType.INLINE;
    }
    return type;
  }

  public void setType(OutputType type) {
    this.type = type;
  }

  public String getC3poCollection() {
    return c3poCollection;
  }

  public void setC3poCollection(String c3poCollection) {
    this.c3poCollection = c3poCollection;
  }

  public BasicDBObject getFilterquery() {
    return filterquery;
  }

  public void setFilterquery(BasicDBObject filterquery) {
    this.filterquery = filterquery;
  }
  
  public abstract MapReduceOutput execute();

}
