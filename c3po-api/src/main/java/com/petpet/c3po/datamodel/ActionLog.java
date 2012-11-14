package com.petpet.c3po.datamodel;

import java.util.Date;

import com.mongodb.BasicDBObject;

public class ActionLog {
  
  public static final String UPDATED_ACTION = "updated";
  
  public static final String PROFILE_ACTION = "profile";

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

  public BasicDBObject getDocument() {
    final BasicDBObject log = new BasicDBObject();
    log.put("collection", this.collection);
    log.put("action", this.action);
    log.put("date", this.date);

    return log;
  }
}
