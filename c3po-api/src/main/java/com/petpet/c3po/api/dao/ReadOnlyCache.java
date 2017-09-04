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
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.PropertyType;

import java.util.List;

/**
 * A simple Read Only cache for {@link Property}, {@link Source} and any kind of
 * other objects.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface ReadOnlyCache {

  /**
   * Retrieves the property designated by the given key. Depending on the
   * implementation it might return null or a new property if there was no
   * property with the given key.
   * 
   * @param key
   *          the key to look for.
   * @return the cached property.
   */
  Property getProperty( String key );

  List<String> getValues(String property);
  
  /**
   * Retrieves the source designated by the given name and version. Depending on
   * the implementation it might return null or a new source if there was no
   * source with the given name and version.
   * 
   * @param name
   *          the name of the source tool.
   * @param version
   *          the version of the too.
   * @return
   */
  Source getSource( String name, String version );

  /**
   * Retrieves the source designated by the given id. Depending on
   * the implementation it might return null or a new source if there was no
   * source with the given name and version.
   *
   * @param id
   * 		  the id of the tool.
   * @return
   */
  Source getSource(String id);

  /**
   * Any other non-persistence layer object that can be cached.
   * 
   * @param key
   *          the key for the cache.
   * @return the object that was cached in memory.
   */
  Object getObject( Object key );
}
