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
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.MetadataRecord;

/**
 * Deserializes {@link DBObject}s into {@link Element} objects.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoElementDeserialzer implements MongoModelDeserializer {

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

    Element e = new Element( coll, uid, name );
    e.setId( obj.get( "_id" ).toString() );
    e.setMetadata( new ArrayList<MetadataRecord>() );

    DBObject meta = (BasicDBObject) obj.get( "metadata" );
    for ( String key : meta.keySet() ) {
      MetadataRecord rec = new MetadataRecord();
      DBObject prop = (DBObject) meta.get( key );
      Property p = this.persistence.getCache().getProperty( key );
      rec.setProperty( p );
      rec.setStatus( prop.get( "status" ).toString() );

      Object value = prop.get( "value" );
      if ( value != null ) {
        rec.setValue( value.toString() );
      }

      // because of boolean and other type conversions.
      List<?> tmp = (List) prop.get( "values" );
      if ( tmp != null ) {
        List<String> values = new ArrayList<String>();
        for ( Object o : tmp ) {
          values.add( o.toString() );
        }
        rec.setValues( values );
      }

      List<String> src = (List<String>) prop.get( "sources" );
      if ( src != null && !src.isEmpty() ) {
        rec.setSources( src );
      }

      e.getMetadata().add( rec );
    }

    return e;
  }

}
