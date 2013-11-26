/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.petpet.c3po.api.model;

import java.util.Date;

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

  /**
   * A constant denoting that the last action over a collection has changed the
   * collection and might have changed the results of any cached data for this
   * collection.
   */
  public static final String UPDATED_ACTION = "updated";

  /**
   * A constant denoting that the last action over a collection has just done
   * some analysis over it and hasn't changed the data.
   */
  public static final String ANALYSIS_ACTION = "analysis";

  /**
   * The collection that was operated on.
   */
  private String collection;

  /**
   * The action that was done.
   */
  private String action;

  /**
   * The date of the action.
   */
  private Date date;

  /**
   * Creates an action log with the current date.
   * 
   * @param collection
   *          the collection that was operated on.
   * @param action
   *          the action done.
   */
  public ActionLog(String collection, String action) {
    this.collection = collection;
    this.action = action;
    this.date = new Date();
  }

  /**
   * Creates an action log.
   * 
   * @param collection
   *          the collection that was operated on.
   * @param action
   *          the action done.
   * @param date
   *          the date when the action was done.
   */
  public ActionLog(String collection, String action, Date date) {
    this( collection, action );
    this.date = date;
  }

  public String getCollection() {
    return collection;
  }

  public void setCollection( String collection ) {
    this.collection = collection;
  }

  public String getAction() {
    return action;
  }

  public void setAction( String action ) {
    this.action = action;
  }

  public Date getDate() {
    return date;
  }

  public void setDate( Date date ) {
    this.date = date;
  }
}
