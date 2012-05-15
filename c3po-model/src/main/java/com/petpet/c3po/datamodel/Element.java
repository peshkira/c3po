package com.petpet.c3po.datamodel;

import java.util.List;

import com.mongodb.BasicDBObject;

public class Element {

  private String name;

  private String uid;

  private List<MetadataRecord> metadata;
  
  public Element(String name, String uid) {
    this.name = name;
    this.uid = uid;
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

  public BasicDBObject getDocument() {
    final BasicDBObject element = new BasicDBObject();
    element.put("name", name);
    element.put("uid", uid);

    final BasicDBObject md = new BasicDBObject();
    for (MetadataRecord r : metadata) {
      md.put(r.getKey(), r.getValue());
    }
    element.put("metadata", md);
    
    return element;
  }
}
