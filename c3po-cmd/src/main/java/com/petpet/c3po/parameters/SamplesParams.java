package com.petpet.c3po.parameters;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.petpet.c3po.command.SamplesCommand;
import com.petpet.c3po.parameters.validation.SampleSizeValidator;

/**
 * The supported paremters for the {@link SamplesCommand}.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class SamplesParams implements Params {

  /**
   * The name of the collection for which to look for samples - required.
   * Supports '-c' and '--collection'.
   */
  @Parameter( names = { "-c", "--collection" }, description = "The name of the collection", required = true )
  private String collection;

  /**
   * The output directory location where to generate a file with the samples
   * identifiers. Default is null and will just print the identifiers to the
   * console.
   * 
   * Supports '-o' and '--outputdir'.
   */
  @Parameter( names = { "-o", "--outputdir" }, description = "The output directory where the samples will be output. If nothing is provided the output is written to the console" )
  private String location = null;

  /**
   * The algorithm to use for the representative selection. Default is size
   * sampling. <br>
   * 
   * Supports '-a' and '--algorithm'.
   */
  @Parameter( names = { "-a", "--algorithm" }, description = "The algorithm that will be used for selecting the samples records. Use one of 'sizesampling', 'syssampling', 'distsampling'" )
  private String algorithm = "sizesampling";

  /**
   * The size of the samples set. Default is 5.
   * 
   * Supports '-s' and '--size'.
   */
  @Parameter( names = { "-s", "--size" }, validateValueWith = SampleSizeValidator.class, description = "The size of the samples set." )
  private int size = 5;

  /**
   * The meta data properties which to use for sample selection. To be used only
   * in combination with '-a distsampling'. <br>
   * 
   * Supports '-props' and '--properties'.
   */
  @Parameter( names = { "-props", "--properties" }, variableArity = true, description = "The list of properties for the 'distsampling' algorithm" )
  private List<String> properties = new ArrayList<String>();

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

  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm( String algorithm ) {
    this.algorithm = algorithm;
  }

  public int getSize() {
    return size;
  }

  public void setSize( int size ) {
    this.size = size;
  }

  public List<String> getProperties() {
    return properties;
  }

  public void setProperties( List<String> properties ) {
    this.properties = properties;
  }
}
