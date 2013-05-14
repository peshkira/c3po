package com.petpet.c3po.parameters;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.petpet.c3po.command.ProfileCommand;
import com.petpet.c3po.parameters.validation.EmptyStringValidator;
import com.petpet.c3po.parameters.validation.SampleSizeValidator;

/**
 * The paremters supported by the {@link ProfileCommand}
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class ProfileParams implements Params {

  /**
   * The collection that will be profiled - required. Supports '-c' and
   * '--collection'.
   */
  @Parameter( names = { "-c", "--collection" }, validateValueWith = EmptyStringValidator.class, required = true, description = "The name of the collection" )
  private String collection;

  /**
   * The output directory location for the generated profile. The default value
   * is the working directory. Supports '-o' and '--outputdir'.
   */
  @Parameter( names = { "-o", "--outputdir" }, description = "The output directory where the profile will be stored" )
  private String location = "";

  /**
   * The algorithm to use. The default is the size strategy. Supports '-a' and
   * '--algorithm'.
   */
  @Parameter( names = { "-a", "--algorithm" }, description = "The algorithm that will be used for selecting the samples records. Supported values are: 'sizesampling', 'syssampling', 'distsampling'" )
  private String algorithm = "sizesampling";

  /**
   * The number of representative samples to look for. The default value is 5.
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

  /**
   * Whether or not to include the element identifiers in the profile. Default
   * is no. Supports '-ie' and '--includeelements'.
   */
  @Parameter( names = { "-ie", "--includeelements" }, arity = 0, description = "If this flag is present, the profile will include a list of element identifiers. Note, that this might be a long list." )
  private boolean includeElements = false;

  public String getCollection() {
    return this.collection;
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

  public boolean isIncludeElements() {
    return includeElements;
  }

  public void setIncludeElements( boolean includeElements ) {
    this.includeElements = includeElements;
  }

}
