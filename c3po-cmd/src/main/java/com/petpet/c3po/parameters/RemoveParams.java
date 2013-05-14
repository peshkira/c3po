package com.petpet.c3po.parameters;

import com.beust.jcommander.Parameter;
import com.petpet.c3po.command.RemoveCommand;
import com.petpet.c3po.parameters.validation.EmptyStringValidator;

/**
 * The supported paremters for the {@link RemoveCommand}.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class RemoveParams implements Params {

  /**
   * The name of the collection to remove - required. <br>
   * Supports '-c' and '--collection'.
   */
  @Parameter( names = { "-c", "--collection" }, validateValueWith = EmptyStringValidator.class, required = true, description = "The name of the collection" )
  private String collection;

  public String getCollection() {
    return collection;
  }

  public void setCollection( String collection ) {
    this.collection = collection;
  }
}
