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

import com.petpet.c3po.api.model.helper.LogEntry;
import com.petpet.c3po.api.model.helper.MetadataRecord;

import java.io.Serializable;
import java.util.*;

/**
 * A domain object class that encapsulates a digital object and its meta data.
 * It consists of a couple of attributes that describe a simple object (usually
 * a file) and a list of specific metadata.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 */

public class Element implements Model, Serializable {

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
    private List<LogEntry> logEntries = new ArrayList<LogEntry>();

    /**
     * Creates an element with the given uid and name.
     *
     * @param uid  the unique identifier of this element.
     * @param name the name of this element.
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
        this(uid, name);
        this.collection = collection;
    }

    public Element() {
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
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

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Removes all records for the given property id and returns a list of all
     * removed meta data records.
     *
     * @param property the id of the property
     * @return returns the records of the element matching this property that were
     * deleted.
     */
    public List<MetadataRecord> removeMetadata(String property) {
        List<MetadataRecord> result = new ArrayList<MetadataRecord>();

        Iterator<MetadataRecord> iterator = this.metadata.iterator();
        while (iterator.hasNext()) {
            MetadataRecord next = iterator.next();
            if (next.getProperty().equals(property)) {
                result.add(next);
                iterator.remove();
            }
        }

        return result;
    }

    /**
     * Split the given {@link MetadataRecord} into one for every {@link Source}.
     * This is necessary, because the MongoDB backend only holds a list of
     * {@link Source}s if all agree on the same value OR one source for each
     * conflicting value. By splitting up {@link MetadataRecord}s that hold
     * several sources, it is granted that all information about {@link Source}s
     * is persisted in the database.
     *
     * @param record The {@link MetadataRecord} to split up.
     */
    public void splitMetadata(MetadataRecord record) {
        if (record.getSources().size() > 1) {
            Iterator<String> sourceIterator = record.getSources().iterator();
            sourceIterator.next(); // the first source will stay in "record"
            while (sourceIterator.hasNext()) {
                String sourceID = sourceIterator.next();
                sourceIterator.remove();

                MetadataRecord newRecord = new MetadataRecord();
                newRecord.setProperty(record.getProperty());
                newRecord.setStatus(record.getStatus());
                newRecord.setValues(record.getValues());
                newRecord.setValues(record.getValues());
                newRecord.setSources(new ArrayList<String>(1));
                newRecord.getSources().add(sourceID);

                this.getMetadata().add(newRecord);
            }
        }
    }

    /**
     * Merge two {@link MetadataRecord}s together if they have the same property
     * and value. The second record gets removed from the list of
     * {@link MetadataRecord}s and its sources are emptied. The sources are added
     * to the first record.
     * <p>
     * If the property and value are not equal (in terms of
     * {@link MetadataRecord#equals(Object)}), nothing happens.
     *
     * @param record1 The {@link MetadataRecord} that will contain the union list of
     *                source entries.
     * @param record2 The {@link MetadataRecord} that will be removed and that will hold
     *                no sources.
     */
    public void mergeMetadata(MetadataRecord record1, MetadataRecord record2) {
        if (record1.getProperty().equals(record2.getProperty())
                && record1.getValues().equals(record2.getValues())) {
            if (this.metadata.remove(record2)) {
                record1.getSources().addAll(record2.getSources());

                // this is necessary to support Drools revert accumulation,
                // otherwise sources get reverted twice
                record2.getSources().clear();
            }
        }
    }

    /**
     * Perform {@link #mergeMetadata(MetadataRecord, MetadataRecord)} on a
     * collection of records.
     *
     * @param record1 The {@link MetadataRecord} that will contain the union list of
     *                source entries.
     * @param records A collection of {@link MetadataRecord}, each of which will be
     *                removed and that will hold no sources.
     */
    public void mergeMetadata(MetadataRecord record1, Collection<MetadataRecord> records) {
        for (MetadataRecord metadataRecord : records) {
            this.mergeMetadata(record1, metadataRecord);
        }
    }

    /**
     * Ignore a {@link MetadataRecord} - currently this simply means that the
     * record is removed from the list of {@link MetadataRecord}s.
     *
     * @param record The {@link MetadataRecord} to be ignored.
     */
    public void ignoreMetadata(MetadataRecord record) {
        this.metadata.remove(record);
    }

    /**
     * Ignore a collection of {@link MetadataRecord}s.
     *
     * @param records The {@link MetadataRecord}s to be ignored.
     * @see Element#ignoreMetadata(MetadataRecord)
     */
    public void ignoreMetadata(Collection<MetadataRecord> records) {
        for (MetadataRecord record : records) {
            this.ignoreMetadata(record);
        }
    }

    public void addLog(String metadataProperty, String metadataValueOld,
                       LogEntry.ChangeType changeType, String ruleName) {
        this.logEntries.add(new LogEntry(metadataProperty, metadataValueOld,
                changeType, ruleName));
    }




    public void addMetadataRecord(String property, String value, String sourceID) {
        //  MetadataRecord metadataRecord = new MetadataRecord(property, value, sourceID);
        //  if (!this.metadata.contains(metadataRecord))
        //  this.metadata.add(metadataRecord);
        for (MetadataRecord metadataRecord : getMetadata()) {
            if (metadataRecord.getProperty().equals(property)) {
                // metadataRecord.getValues().add(value);
                // metadataRecord.getSources().add(sourceID);
                metadataRecord.getSourcedValues().put(sourceID, value);
                return;
            }
        }
        MetadataRecord metadataRecord = new MetadataRecord(property, value, sourceID);
        getMetadata().add(metadataRecord);

    }


}
