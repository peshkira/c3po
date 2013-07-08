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

/**
 * A simple interface for serializing objects into Mongo {@link DBObject}s.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface MongoModelSerializer {

  /**
   * Serializes the given object to a {@link DBObject}. It is up to the backend
   * provider how the POJO will be mapped into the mongo schema.
   * 
   * @param object
   *          the object to serialize.
   * @return the serialized {@link DBObject}
   */
  DBObject serialize( Object object );

}
