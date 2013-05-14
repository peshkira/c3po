package com.petpet.c3po.parameters;

import com.beust.jcommander.Parameter;
import com.petpet.c3po.command.ExportCommand;

/**
 * The supported parameters for the {@link ExportCommand}
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class ExportParams implements Params {

  /**
   * The name of the collection - required. Supports '-c' and '--collection'.
   */
  @Parameter( names = { "-c", "--collection" }, description = "The name of the collection", required = true )
  private String collection;

  /**
   * The output directory location - optional. The default is the working
   * directory. Supports '-o' and '--outputdir'.
   */
  @Parameter( names = { "-o", "--outputdir" }, description = "The output directory where the profile will be stored" )
  private String location = "";

  public String getCollection() {
    return collection;
  }

  public void setCollection( String collection ) {
    this.collection = collection;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation( String location ) {
    this.location = location;
  }
}
