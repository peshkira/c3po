package com.petpet.c3po.api.model;

import java.util.Date;

import com.mongodb.BasicDBObject;

/**
 * A basic model class that stores the information of the last action done on a
 * collection and the date that the action was executed. It is used to retrieve
 * information about the last action over a collection in order to know, whether
 * or not additional actions are required.
 * 
 * For example, if a collections is marked as updated, then no cached
 * results/profiles for this collection can be reused and they have to be
 * regenerated.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class ActionLog implements Model {

  public static final String UPDATED_ACTION = "updated";

  public static final String ANALYSIS_ACTION = "analysis";

  private String collection;

  private String action;

  private Date date;

  public ActionLog(String collection, String action) {
    this.collection = collection;
    this.action = action;
    this.date = new Date();
  }

  public ActionLog(String collection, String action, Date date) {
    this(collection, action);
    this.date = date;
  }

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  @Deprecated
  public BasicDBObject getDocument() {
    final BasicDBObject log = new BasicDBObject();
    log.put("collection", this.collection);
    log.put("action", this.action);
    log.put("date", this.date);

    return log;
  }
}
