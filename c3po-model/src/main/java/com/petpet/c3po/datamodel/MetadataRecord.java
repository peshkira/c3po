package com.petpet.c3po.datamodel;

import java.util.List;

public class MetadataRecord {
  
  public static enum Status {
    OK, SINGLE, PARTIAL, CONFLICT
  }

  private String pRef;
  
  private String value;
  
  private String status;
  
  private List<String> sources;
  
  public MetadataRecord() {
    
  }
  
  public MetadataRecord(String pRef, String value) {
    this.pRef = pRef;
    this.value = value;
    this.status = Status.SINGLE.name();
  }
  
  public MetadataRecord(String pRef, String value, Status status) {
    this(pRef, value);
    this.status = status.name();
  }

  public String getKey() {
    return pRef;
  }

  public void setKey(String key) {
    this.pRef = key;
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
