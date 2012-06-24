package com.petpet.c3po.datamodel;

import java.util.UUID;

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
  
  private String id;

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
   * The parent filter of this filter.
   */
  private Filter parent;

  /**
   * The filter that has to be applied on the matching part of the collection.
   */
  private String matching;

  /**
   * The filter that has to be applied on the nonmatching part of the
   * collection.
   */
  private String nonmatching;

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
    this.parent = null;
    this.id = UUID.randomUUID().toString();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public Filter getParent() {
    return parent;
  }

  public void setParent(Filter parent) {
    this.parent = parent;
  }

  public String getMatching() {
    return matching;
  }

  public void setMatching(String matching) {
    this.matching = matching;
  }

  public String getNonmatching() {
    return nonmatching;
  }

  public void setNonmatching(String nonmatching) {
    this.nonmatching = nonmatching;
  }
  
  public DBObject getDocument() {
    final BasicDBObject filter = new BasicDBObject("_id", this.id);
    filter.put("collection", collection);
    filter.put("property", property);
    filter.put("value", value);
    if(parent != null) {
      filter.put("parent", parent.getDocument());
    }
    
    filter.put("matching", matching);
    filter.put("nonmatching", nonmatching);
    
    return filter;
  }
}
