package com.petpet.c3po.parameters;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.petpet.c3po.parameters.validation.EmptyStringValidator;
import com.petpet.c3po.parameters.validation.SampleSizeValidator;

public class ProfileParams implements Params {

  @Parameter(names = { "-c", "--collection" }, validateValueWith = EmptyStringValidator.class, required = true, description = "The name of the collection")
  private String collection;

  @Parameter(names = { "-o", "--outputdir" }, description = "The output directory where the profile will be stored")
  private String location = "";
  
  @Parameter(names = {"-a", "--algorithm"}, description = "The algorithm that will be used for selecting the samples records")
  private String algorithm = "sizesampling";
  
  @Parameter(names = {"-s", "--size"}, validateValueWith = SampleSizeValidator.class,description = "The size of the samples set. Default is 5")
  private int size = 5;
  
  @Parameter(names = {"-props", "--properties"}, variableArity = true)
  private List<String> properties;
  
  @Parameter(names = { "-ie", "--includeelements" }, arity = 0, description = "Whether or not to gather recursively")
  private boolean includeElements = false;

  public String getCollection() {
    return this.collection;
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

  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public List<String> getProperties() {
    return properties;
  }

  public void setProperties(List<String> properties) {
    this.properties = properties;
  }

  public boolean isIncludeElements() {
    return includeElements;
  }

  public void setIncludeElements(boolean includeElements) {
    this.includeElements = includeElements;
  }

}
