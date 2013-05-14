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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

}
