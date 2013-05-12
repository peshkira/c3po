package com.petpet.c3po.parameters;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.petpet.c3po.parameters.validation.SampleSizeValidator;

public class SamplesParams implements Params {

  @Parameter(names = { "-c", "--collection" }, description = "The name of the collection", required = true)
  private String collection;

  @Parameter(names = { "-o", "--outputdir" }, description = "The output directory where the profile will be stored. If nothing is provided the output is written to the console")
  private String location = null;
  
  @Parameter(names = {"-a", "--algorithm"}, description = "The algorithm that will be used for selecting the samples records. Use one of 'sizesampling', 'syssampling', 'distsampling'")
  private String algorithm = "sizesampling";
  
  @Parameter(names = {"-s", "--size"}, validateValueWith = SampleSizeValidator.class,description = "The size of the samples set.")
  private int size = 5;
  
  @Parameter(names = {"-props", "--properties"}, variableArity = true, description = "The list of properties for the 'distsampling' algorithm")
  private List<String> properties = new ArrayList<String>();

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
}
