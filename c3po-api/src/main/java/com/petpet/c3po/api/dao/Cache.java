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

import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.PropertyType;

/**
 * A simple cache that allows easy retrieval of common objects as properties and
 * sources of meta data, but also putting any kind of object or clearing it..
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface Cache extends ReadOnlyCache {

  /**
   * Retrieves the property designated by the given key and type. Depending on
   * the implementation it might return null or a new property if there was no
   * property with the given key. If a property with the given key was already
   * present, it will be returned no matter if the type matches.
   * 
   * @param key
   *          the key to look for.
   * @param type
   *          the type of the property.
   * @return the cached property.
   */
  Property getProperty( String key, PropertyType type );

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
