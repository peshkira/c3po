package com.petpet.c3po.datamodel;

import java.util.UUID;

import com.mongodb.BasicDBObject;

public class Property {

  public enum PropertyType {
    STRING, BOOL, INTEGER, FLOAT, DATE, ARRAY
  }

  private String id;

  private String key;

  private String name;

  private String description;

  private String type;

  public Property() {

  }

  public Property(String key, String name) {
    this.id = UUID.randomUUID().toString();
    this.setKey(key);
    this.setName(name);
    this.setType(PropertyType.STRING.name());
  }

  public Property(String key, String name, PropertyType type) {
    this(key, name);
    this.setType(type.name());
  }

  public Property(String key, String name, PropertyType type, String desc) {
    this(key, name, type);
    this.setDescription(desc);
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public BasicDBObject getDocument() {
    final BasicDBObject property = new BasicDBObject();
    property.put("_id", this.getId());
    property.put("key", this.getKey());
    property.put("type", this.getType());

    return property;
  }

}
