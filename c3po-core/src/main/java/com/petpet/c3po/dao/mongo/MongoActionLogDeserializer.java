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

import java.util.Date;

import com.mongodb.DBObject;
import com.petpet.c3po.api.model.ActionLog;

/**
 * Deserializes {@link DBObject}s to {@link ActionLog} objects.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoActionLogDeserializer implements MongoModelDeserializer {

  /**
   * Deserializes the {@link DBObject} to an {@link ActionLog}. Note, that if
   * the passed object is null or not a {@link DBObject} null is returned.
   */
  @Override
  public ActionLog deserialize( Object object ) {
    if ( object == null || !(object instanceof DBObject) ) {
      return null;
    }

    DBObject dbObject = (DBObject) object;

    return this.parseActionLog( dbObject );
  }

  /**
   * Parses the db object to an action log.
   * 
   * @param object
   *          the object to parse.
   * @return the action log.
   */
  private ActionLog parseActionLog( DBObject object ) {
    String c = (String) object.get( "collection" );
    String a = (String) object.get( "action" );
    Date d = (Date) object.get( "date" );

    return new ActionLog( c, a, d );
  }

}
