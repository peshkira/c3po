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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.model.ActionLog;

/**
 * Serializes an {@link ActionLog} object to a mongo {@link DBObject}.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoActionLogSerializer implements MongoModelSerializer {

  /**
   * Maps the given {@link ActionLog} to a mongo document. Note that if the
   * given object is null or not of type {@link ActionLog}, then null is
   * returned.
   */
  @Override
  public DBObject serialize( Object object ) {
    BasicDBObject log = null;

    if ( object != null && object instanceof ActionLog ) {
      ActionLog l = (ActionLog) object;

      log = new BasicDBObject();

      if ( l.getCollection() != null && !l.getCollection().equals( "" ) ) {
        log.put( "collection", l.getCollection() );
      }

      if ( l.getAction() != null && !l.getAction().equals( "" ) ) {
        log.put( "action", l.getAction() );
      }

      if ( l.getDate() != null && !l.getDate().equals( "" ) ) {
        log.put( "date", l.getDate() );
      }

    }

    return log;

  }

}
