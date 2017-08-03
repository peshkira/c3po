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
package com.petpet.c3po.dao.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;

/**
 * Deserializes {@link DBObject}s into {@link Element} objects.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoElementDeserialzer implements MongoModelDeserializer {
  public MongoElementDeserialzer() {
  }

/*  *//**
   * Deserializes {@link DBObject}s to elements.
   *//*
  @Override
  public Element deserialize( Object object ) {
    if ( object == null || !(object instanceof DBObject) ) {
      return null;
    }

    DBObject dbObject = (DBObject) object;

    return this.parseElement( dbObject );

  }

  /**
   * Parses the element from a db object returned by the db.
   *
   * @param obj
   *          the object to parse.
   * @return the Element.
   *//*
  private Element parseElement( final DBObject obj ) {
    Gson gson=new Gson();
   Element element = gson.fromJson(obj.toString(), Element.class);
    return element;
  }*/
  /**
   * Needs a reference to the persistence layer in order to obtain the sources
   * for each meta data record.
   */
  private PersistenceLayer persistence;

  public MongoElementDeserialzer(PersistenceLayer p) {
    this.persistence = p;
  }

  /**
   * Deserializes {@link DBObject}s to elements.
   */
  @Override
  public Element deserialize( Object object ) {
    if ( object == null || !(object instanceof DBObject) ) {
      return null;
    }

    DBObject dbObject = (DBObject) object;

    return this.parseElement( dbObject );

  }

  /**
   * Parses the element from a db object returned by the db.
   *
   * @param obj
   *          the object to parse.
   * @return the Element.
   */
  private Element parseElement( final DBObject obj ) {
    String coll = (String) obj.get( "collection" );
    String uid = (String) obj.get( "uid" );
    String name = (String) obj.get( "name" );
    List<DBObject> metadata = (List<DBObject>) obj.get("metadata");
    Element e = new Element( coll, uid, name );
    if (obj.get( "_id" )!=null)
      e.setId( obj.get( "_id" ).toString() );
    ArrayList<MetadataRecord> metadataRecords = new ArrayList<>();
    for (DBObject meta : metadata) {
      MetadataRecord mr=new MetadataRecord();
      String property = (String) meta.get("property");
      String status =(String) meta.get("status");
      List<DBObject> sourcedValues = (List<DBObject>) meta.get("sourcedValues");
      HashMap<String,String> hmap=new HashMap<String, String>();
      for (DBObject sourcedValue : sourcedValues) {
        String sourceID = (String) sourcedValue.get("source");
        String value = sourcedValue.get("value").toString();
        hmap.put(sourceID,value);
      }
      mr.setProperty(property);
      mr.setStatus(status);
      mr.setSourcedValues(hmap);
      metadataRecords.add(mr);
    }

    e.setMetadata(metadataRecords  );

    /*DBObject meta =  obj;
    for ( String key : meta.keySet() ) {
      MetadataRecord rec = new MetadataRecord();

      if (key.equals("_id") ||key.equals("collection") || key.equals("name") || key.equals("uid") )
        continue;
      DBObject prop = (DBObject) meta.get( key );
      Property p = this.persistence.getCache().getProperty( key );
      rec.setProperty( p.getKey() );
      rec.setStatus( prop.get( "status" ).toString() );

      //Object value = prop.get( "value" );
      //if ( value != null ) {
      //  rec.setValue( value.toString() );
      //}

      // because of boolean and other type conversions.
      List<?> tmp = (List) prop.get( "sourcedValues" );
      if ( tmp != null ) {
        List<String> values = new ArrayList<String>();
        for ( Object o : tmp ) {
          values.add( o.toString() );
        }
        rec.setValues( values );
      }

      List<String> src = (List<String>) prop.get( "sources" );
      if ( src != null && !src.isEmpty() ) {
        rec.setSources( src);
      }

      e.getMetadata().add( rec );
    }*/

    return e;
  }

}
