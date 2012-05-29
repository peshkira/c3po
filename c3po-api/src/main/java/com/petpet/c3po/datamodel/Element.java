package com.petpet.c3po.datamodel;

import java.util.List;

public class Element {
  
  private String collection;

  private String name;

  private String uid;

  private List<MetadataRecord> metadata;
  
  public Element(String uid, String name) {
    this.uid = uid;
    this.name = name;
  }
  
  public Element(String collection, String uid, String name) {
    this(uid, name);
    this.collection = collection;
  }
  
  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public List<MetadataRecord> getMetadata() {
    return metadata;
  }

  public void setMetadata(List<MetadataRecord> metadata) {
    this.metadata = metadata;
  } 

}
