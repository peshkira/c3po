package com.petpet.c3po.datamodel;

import java.util.UUID;

import com.mongodb.BasicDBObject;

public class Source {

  private String id;

  private String name;

  private String version;

  public Source() {

  }

  public Source(String name, String version) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
    this.version = version;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public BasicDBObject getDocument() {
    final BasicDBObject source = new BasicDBObject();

    source.put("_id", this.getId());
    source.put("name", this.getName());
    source.put("version", this.getVersion());

    return source;
  }

}
