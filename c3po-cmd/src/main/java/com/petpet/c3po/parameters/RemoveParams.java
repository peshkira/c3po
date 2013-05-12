package com.petpet.c3po.parameters;

import com.beust.jcommander.Parameter;
import com.petpet.c3po.parameters.validation.EmptyStringValidator;

public class RemoveParams implements Params {

  @Parameter(names = { "-c", "--collection" }, validateValueWith = EmptyStringValidator.class, required = true, description = "The name of the collection")
  private String collection;

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }
}
