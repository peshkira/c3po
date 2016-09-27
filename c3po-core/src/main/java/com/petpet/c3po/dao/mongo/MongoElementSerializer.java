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
import com.mongodb.util.JSON;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.LogEntry;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.MetadataRecord.Status;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;

/**
 * Serializes {@link Element}s into {@link DBObject}s.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoElementSerializer implements MongoModelSerializer {

  /**
   * Maps the given element object to a mongo NoSQL schema, where every element
   * is represented as a single document wrapping all its meta data records.
   * 
   * Note that if the passed object is null not of type {@link Element} null is
   * returned.
   */
  @Override
  public DBObject serialize( Object object ) {

    /*BasicDBObject document = null;
    if ( object != null && object instanceof Element ) {
      Element element = (Element) object;
      Gson gson=new Gson();
      String s = gson.toJson(element);
      document = (BasicDBObject) JSON.parse(s);
    }
    return document;
*/
    BasicDBObject document = null;

    if ( object != null && object instanceof Element ) {
      Element element = (Element) object;
      element.updateStatus();
      document = new BasicDBObject();
      if ( element.getName() != null && !element.getName().equals( "" ) ) {
        document.put( "name", element.getName() );
      }

      if ( element.getUid() != null && !element.getUid().equals( "" ) ) {
        document.put( "uid", element.getUid() );
      }

      if ( element.getCollection() != null && !element.getCollection().equals( "" ) ) {
        document.put( "collection", element.getCollection() );
      }

     // BasicDBObject meta = new BasicDBObject();
      for ( MetadataRecord r : element.getMetadata() ) {
        BasicDBObject key = new BasicDBObject();
        key.put( "status", r.getStatus() );
        key.put( "sources", r.getSources() );
        String property = r.getProperty();
        String type = Configurator.getDefaultConfigurator().getPersistence().getCache().getProperty(property).getType();
        List<Object> objectValues=new ArrayList<Object>();
        for (String s : r.getValues()) {
          objectValues.add(DataHelper.getTypedValue(type,s));
        }
        key.put( "values", objectValues );
        document.put( r.getProperty(), key );


       /* if ( r.getStatus().equals( Status.CONFLICT.name() ) ) {
          BasicDBObject conflicting;
          List<Object> values;
          List<Object> sources;
          if ( document.containsField( r.getProperty() ) ) {
            conflicting = (BasicDBObject) document.get( r.getProperty() );
            values = (List<Object>) conflicting.get( "values" );
            if ( values == null ) {
              values = new ArrayList<Object>();
              values.add( conflicting.get( "value" ) );
            }
            sources = (List<Object>) conflicting.get( "sources" );
            values.addAll(r.getValues());// DataHelper.getTypedValue( r.getProperty().getType(), r.getValue() ) );
            sources.add( r.getSources().get( 0 ) );

          } else {
            conflicting = new BasicDBObject();
            values = new ArrayList<Object>();
            sources = new ArrayList<Object>();

            if ( r.getValues() == null || r.getValues().equals( "" ) ) {
             // for ( String s : r.getValues() ) {
                values.addAll(r.getValues());// DataHelper.getTypedValue( r.getProperty().getType(), s ) );
            //  }
              sources.addAll( r.getSources() );
            } else {

              values.add( DataHelper.getTypedValue( r.getProperty().getType(), r.getValue() ) );
              sources.add( r.getSources().get( 0 ) );
            }
          }

          conflicting.put( "values", values );
          conflicting.put( "sources", sources );
          conflicting.put( "status", r.getStatus() );
          document.put( r.getProperty().getId(), conflicting );

        } else {
          key.put( "value", DataHelper.getTypedValue( r.getProperty().getType(), r.getValue() ) );
          key.put( "sources", r.getSources() );
          document.put( r.getProperty().getId(), key );
        }
*/
      }

      //if ( !meta.keySet().isEmpty() ) {
      //  document.put( "metadata", meta );
      //}

      BasicDBObject logEntries = new BasicDBObject();

      for (LogEntry logEntry : element.getLogEntries()) {

        logEntries.put("property", logEntry.getMetadataProperty());
        logEntries.put("valueOld", logEntry.getMetadataValueOld());
        logEntries.put("changeType", logEntry.getChangeType().name());
        logEntries.put("ruleName", logEntry.getRuleName());
      }
      if (logEntries.size()!=0)
        document.put("log", logEntries);

    }

    return document;
  }

}
