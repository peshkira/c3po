package com.petpet.c3po.datamodel;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * This class is used to filter a c3po collection on many levels. It has a
 * matching and non-matching child filters that can be applied to the current
 * partition of the collection.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class Filter {
  
  private String descriminator;

  /**
   * The collection that is filtered.
   */
  private String collection;

  /**
   * The property that is filtered by this filter (e.g. mimetype).
   */
  private String property;

  /**
   * The value of the property by which the filter is partitioning (e.g.
   * application/pdf).
   */
  private String value;

  
  /**
   * Creates a default root filter. This means that the filter has no parent
   * filter.
   * 
   * @param collection
   *          the collection to filter.
   * @param property
   *          the property to apply.
   * @param value
   *          the value of the property to apply for this filter.
   */
  public Filter(String collection, String property, String value) {
    this.collection = collection;
    this.property = property;
    this.value = value;
  }

  public String getDescriminator() {
    return descriminator;
  }

  public void setDescriminator(String id) {
    this.descriminator = id;
  }

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public DBObject getDocument() {
    final BasicDBObject filter = new BasicDBObject();
    filter.put("descriminator", this.descriminator);
    filter.put("collection", collection);
    filter.put("property", property);
    filter.put("value", value);
    
    return filter;
  }
}
