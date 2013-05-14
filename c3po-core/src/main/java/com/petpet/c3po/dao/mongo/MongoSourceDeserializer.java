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

import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Source;

public class MongoSourceDeserializer implements MongoModelDeserializer {

  /**
   * Deserializes a {@link DBObject} into a {@link Source} object.
   */
  @Override
  public Object deserialize( Object object ) {

    if ( object == null || !(object instanceof DBObject) ) {
      return null;
    }
    DBObject dbObject = (DBObject) object;

    String id = (String) dbObject.get( "_id" );
    String name = (String) dbObject.get( "name" );
    String version = (String) dbObject.get( "version" );

    Source s = new Source();
    s.setId( id );
    s.setName( name );
    s.setVersion( version );

    return s;
  }

}
