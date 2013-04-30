package com.petpet.c3po.api.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.MetadataRecord.Status;
import com.petpet.c3po.common.Constants;

/**
 * A domain object class that encapsulates an element document. It consists of a
 * couple of attributes that describe a simple object (usually a file) and a
 * list of specific metadata.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 */

public class Element implements Model {

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
   * Creates an element with the given uid and name.
   * 
   * @param uid
   * @param name
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * Removes all records for the given property id and returs a 
   * list of all removed metadata records.
   * 
   * @param property
   *          the id of the property
   * @return returns the records of the element matching this property that were deleted.
   */
  public List<MetadataRecord> removeMetadata(String property) {
    List<MetadataRecord> result = new ArrayList<MetadataRecord>();
    
    Iterator<MetadataRecord> iterator = this.metadata.iterator();
    while(iterator.hasNext()) {
      MetadataRecord next = iterator.next();
      if (next.getProperty().getId().equals(property)) {
        result.add(next);
        iterator.remove();
      }
    }
    
    return result;
  }

  /**
   * Tries to parse a creation date out of the name of the current element. Some
   * sources include a timestamp in the name. The usage of this method can be
   * configured through the adaptor config parameter
   * {@link Constants#OPT_INFER_DATE}
   * 
   * If the name is not set or it does not include a timestamp, the method does
   * nothing.
   * 
   * @param created
   *          the property for the creation date.
   */
  /*
   * experimental only for the SB archive
   */
  public void extractCreatedMetadataRecord(Property created) {
    if (this.name != null) {

      String[] split = name.split("-");

      if (split.length > 2) {
        String date = split[2];

        try {
          Long.valueOf(date);

          MetadataRecord c = new MetadataRecord(created, date);
          this.metadata.add(c);

        } catch (NumberFormatException nfe) {
          // if the value is not a number then it is something else and not a
          // year, skip the inference.

        }
      }
    }
  }


  /**
   * Gets the BSON Object representation that is stored within the mongo
   * database.
   * 
   * @return the document of this element.
   */
  @Deprecated
  public BasicDBObject getDocument() {
    return null;
  }

}
