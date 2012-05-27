package com.petpet.c3po.datamodel;

import java.util.ArrayList;
import java.util.List;

public class MetadataRecord {
  
  public static enum Status {
    OK, SINGLE_RESULT, PARTIAL, CONFLICT
  }

  private Property property;
  
  private String value;
  
  private String status;
  
  private List<String> sources;
  
  public MetadataRecord() {
    this.sources = new ArrayList<String>();
    this.status = Status.OK.name();
  }
  
  public MetadataRecord(Property p, String value) {
    this();
    this.property = p;
    this.value = value;
    this.status = Status.SINGLE_RESULT.name();
  }
  
  public MetadataRecord(Property p, String value, Status status) {
    this(p, value);
    this.status = status.name();
  }

  public Property getProperty() {
    return this.property;
  }

  public void setProperty(Property p) {
    this.property = p;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<String> getSources() {
    return sources;
  }

  public void setSources(List<String> sources) {
    this.sources = sources;
  }
}
