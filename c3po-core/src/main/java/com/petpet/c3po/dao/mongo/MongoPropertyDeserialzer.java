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
import com.petpet.c3po.api.model.Property;

/**
 * Deserializes the mongo DBObject into a Property object.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoPropertyDeserialzer implements MongoModelDeserializer {

  /**
   * Deserializes the given {@link DBObject} into a property.
   */
  @Override
  public Property deserialize( Object object ) {
    if ( object == null || !(object instanceof DBObject) ) {
      return null;
    }

    DBObject dbObject = (DBObject) object;

    return this.parseProperty( dbObject );
  }

  /**
   * Parses the property.
   * 
   * @param obj
   * @return
   */
  private Property parseProperty( DBObject obj ) {
    Property result = null;
    if ( obj != null ) {
      String key = (String) obj.get( "key" );
      String type = (String) obj.get( "type" );

      result = new Property( key );
      result.setType( type );
    }

    return result;
  }

}
