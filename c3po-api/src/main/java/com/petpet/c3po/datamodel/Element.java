package com.petpet.c3po.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.bson.BSONObject;
import org.bson.types.BasicBSONList;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

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

//    final List<BasicDBObject> meta = new ArrayList<BasicDBObject>();
    for (MetadataRecord r : metadata) {
      final BasicDBObject md = new BasicDBObject();
      
      md.put("value", r.getValue());
      md.put("status", r.getStatus());
      md.put("sources", r.getSources());
      element.put(r.getPRef(), md);
    }
//    element.put("metadata", meta);
    
    return element;
  }
}
