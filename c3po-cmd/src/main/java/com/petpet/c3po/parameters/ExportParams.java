package com.petpet.c3po.parameters;

import com.beust.jcommander.Parameter;

public class ExportParams implements Params {

  @Parameter(names = { "-c", "--collection" }, description = "The name of the collection", required = true)
  private String collection;

  @Parameter(names = { "-o", "--outputdir" }, description = "The output directory where the profile will be stored")
  private String location = "";

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }
}
