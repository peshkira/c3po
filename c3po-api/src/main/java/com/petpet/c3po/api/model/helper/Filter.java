package com.petpet.c3po.api.model.helper;

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
  
  @Deprecated
  private String descriminator;

  /**
   * The collection that is filtered.
   */
  @Deprecated
  private String collection;

  /**
   * The property that is filtered by this filter (e.g. mimetype).
   */
  @Deprecated
  private String property;

  /**
   * The value of the property by which the filter is partitioning (e.g.
   * application/pdf).
   */
  @Deprecated
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
  @Deprecated
  public Filter(String collection, String property, String value) {
    this.collection = collection;
    this.property = property;
    this.value = value;
  }

  @Deprecated
  public String getDescriminator() {
    return descriminator;
  }

  @Deprecated
  public void setDescriminator(String id) {
    this.descriminator = id;
  }

  @Deprecated
  public String getCollection() {
    return collection;
  }

  @Deprecated
  public void setCollection(String collection) {
    this.collection = collection;
  }
  
  @Deprecated
  public String getProperty() {
    return property;
  }

  @Deprecated
  public void setProperty(String property) {
    this.property = property;
  }

  @Deprecated
  public String getValue() {
    return value;
  }

  @Deprecated
  public void setValue(String value) {
    this.value = value;
  }

  @Deprecated
  public DBObject getDocument() {
    final BasicDBObject filter = new BasicDBObject();
    filter.put("descriminator", this.descriminator);
    filter.put("collection", collection);
    filter.put("property", property);
    filter.put("value", value);
    
    return filter;
  }
}
