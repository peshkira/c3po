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

import java.util.*;

import com.petpet.c3po.api.model.helper.LogEntry;
import com.petpet.c3po.api.model.helper.MetadataRecord;

/**
 * A domain object class that encapsulates a digital object and its meta data.
 * It consists of a couple of attributes that describe a simple object (usually
 * a file) and a list of specific metadata.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 */

public class Element implements Model {

  /**
   * A back-end related identifier of this object.
   */
  private String id;

  /**
   * The collection to which the current element belongs.
   */
  private String collection;

  /**
   * Some non-unique name of this element.
   */
  private String name;

  /**
   * Some unique identifier of this element that references the original file
   * back in the source.
   */
  private String uid;

  /**
   * A list of {@link MetadataRecord} info.
   */
  private List<MetadataRecord> metadata;

  /**
   * A list of {@link LogEntry} records
   */
  private List<LogEntry> logEntries = new LinkedList<LogEntry>();

  /**
   * Creates an element with the given uid and name.
   * 
   * @param uid
   *          the unique identifier of this element.
   * @param name
   *          the name of this element.
   */
  public Element(String uid, String name) {
    this.uid = uid;
    this.name = name;
    this.metadata = new ArrayList<MetadataRecord>();
  }

  /**
   * Creates an element with the given uid, name and collection.
   * 
   * @param collection
   * @param uid
   * @param name
   */
  public Element(String collection, String uid, String name) {
    this( uid, name );
    this.collection = collection;
  }

  public String getCollection() {
    return collection;
  }

  public void setCollection( String collection ) {
    this.collection = collection;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getUid() {
    return uid;
  }

  public void setUid( String uid ) {
    this.uid = uid;
  }

  public List<MetadataRecord> getMetadata() {
    return metadata;
  }

  public void setMetadata( List<MetadataRecord> metadata ) {
    this.metadata = metadata;
  }

  public List<LogEntry> getLogEntries() {
    return logEntries;
  }

  public void setLogEntries(List<LogEntry> logEntries) {
    this.logEntries = logEntries;
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  /**
   * Removes all records for the given property id and returns a list of all
   * removed meta data records.
   * 
   * @param property
   *          the id of the property
   * @return returns the records of the element matching this property that were
   *         deleted.
   */
  public List<MetadataRecord> removeMetadata( String property ) {
    List<MetadataRecord> result = new ArrayList<MetadataRecord>();

    Iterator<MetadataRecord> iterator = this.metadata.iterator();
    while ( iterator.hasNext() ) {
      MetadataRecord next = iterator.next();
      if ( next.getProperty().getId().equals( property ) ) {
        result.add( next );
        iterator.remove();
      }
    }

    return result;
  }

  public void splitMetadata(MetadataRecord record) {
    if (record.getSources().size() > 1) {
      Iterator<String> iterator = record.getSources().iterator();
      iterator.next(); // the first source will stay in "record"
      while (iterator.hasNext()) {
        String sourceID = iterator.next();
        iterator.remove();

        MetadataRecord newRecord = new MetadataRecord();
        newRecord.setProperty(record.getProperty());
        newRecord.setStatus(record.getStatus());
        newRecord.setValue(record.getValue());
        newRecord.setValues(record.getValues());
        newRecord.setSources(new ArrayList<String>(1));
        newRecord.getSources().add(sourceID);

        this.getMetadata().add(newRecord);
      }
    }
  }

  public void mergeMetadata(MetadataRecord record1, MetadataRecord record2) {
    if (record1.getProperty().getId().equals(record2.getProperty().getId())
        && record1.getValue().equals(record2.getValue())) {
      if (this.metadata.remove(record2)) {
        record1.getSources().addAll(record2.getSources());

        // this is necessary to support Drools revert accumulation,
        // otherwise sources get reverted twice
        record2.getSources().clear();
      }
    }
  }

  public void mergeMetadata(MetadataRecord record1, List<MetadataRecord> records) {
    for (MetadataRecord metadataRecord : records) {
      this.mergeMetadata(record1, metadataRecord);
    }
  }

  public void ignoreMetadata(MetadataRecord record) {
    this.metadata.remove(record);
  }

  public void ignoreMetadata(Collection<MetadataRecord> records) {
    for (MetadataRecord record : records) {
      this.ignoreMetadata(record);
    }
  }

  public void addLog(MetadataRecord record, LogEntry.ChangeType changeType,
                     String ruleName) {
    this.logEntries.add(new LogEntry(record.getProperty().getId(), record
        .getValue(), changeType, ruleName));
  }

  public void addLog(String metadataProperty, String metadataValueOld,
                     LogEntry.ChangeType changeType, String ruleName) {
    this.logEntries.add(new LogEntry(metadataProperty, metadataValueOld,
        changeType, ruleName));
  }

}
