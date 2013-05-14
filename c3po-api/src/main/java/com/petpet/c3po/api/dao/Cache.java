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
package com.petpet.c3po.api.dao;

/**
 * A simple cache that allows easy retrieval of common objects as properties and
 * sources of meta data, but also putting any kind of object or clearing it..
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface Cache extends ReadOnlyCache {

  /**
   * Puts the given value to the given key. This object does not have to be
   * stored to the persistence layer.
   * 
   * @param key
   *          the key of the object
   * @param value
   *          the value to cache.
   */
  void put( Object key, Object value );

  /**
   * Clears the cache.
   */
  void clear();
}
