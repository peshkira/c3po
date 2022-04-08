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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.petpet.c3po.api.model.Model;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

/**
 * The persistence layer interface offers some common methods for interacting
 * with the underlying database. All implementing classes require a default
 * constructor.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface PersistenceLayer {

  /**
   * Clears the current cache of the application. This includes the
   * {@link Cache}, but also any other cached information that the backend
   * provider might have stored (e.g. cached statistics and aggregations).
   */
  void clearCache();

  /**
   * This method will be called once before the applications ends running.
   */
  void close() throws C3POPersistenceException;

  /**
   * Counts the number of objects of the given type that match the filter. If
   * the filter is null, the count of all objects of that type is retrieved.
   * 
   * @param clazz
   *          the type of objects
   * @param filter
   *          the filter.
   * @return the number of objects corresponding to that type and filter.
   */
  <T extends Model> long count( Class<T> clazz, Filter filter );

  /**
   * This method will be called upon initialisation of the application. The map
   * will contain all configurations (loaded from the config file) starting with
   * the prefix 'db'. Example: db.host=192.168.0.1
   * 
   * @param config
   *          the configuration
   */
  void establishConnection( Map<String, String> config ) throws C3POPersistenceException;

  /**
   * Finds objects corresponding to the supplied filter and type. The type has
   * to implement the Model interface. Retrieves an iterator for the objects.
   * 
   * Note that depending on the underlying implementation this method might
   * fetch a lot of data from the database.
   * 
   * @param clazz
   *          the generic type of the iterator
   * @param filter
   *          the optional filter.
   * @return a typed iterator for the given type of objects.
   */
  <T extends Model> Iterator<T> find( Class<T> clazz, Filter filter );

  /**
   * Returns the distinct values for the given property (in string form). If the
   * passed filter is not null, then it is applied on the collection before
   * 
   * @param clazz
   *          the type of the objects to look at.
   * @param f
   *          the field for which the distinct values have to be applied
   * @param filter
   *          the filter that has to be applied on the data before the distinct
   *          operation is executed
   * @return the distinct values for a given property
   */
  <T extends Model> List<String> distinct( Class<T> clazz, String f, Filter filter );

  /**
   * Gets a cache object.
   * 
   * @return the cache of the application.
   */
  Cache getCache();


  /**
   * Inserts the given object to the underlying data store.
   * 
   * @param object
   *          the object to store.
   */
  <T extends Model> void insert( T object );

  /**
   * Whether or not there is an established connection to the database.
   * 
   * @return true if there is a connection, false otherwise.
   */
  boolean isConnected();

  /**
   * Removes the objects of the given type that match the filter from the
   * underlying data store. If the filter is null, then all objects are removed.
   * 
   * @param clazz
   *          the type of object to remove
   * @param filter
   *          the filter to match. Can be null.
   */
  <T extends Model> void remove( Class<T> clazz, Filter filter );

  /**
   * Removes the given object from the underlying data store.
   * 
   * @param object
   *          the object to remove.
   */
  <T extends Model> void remove( T object );

  /**
   * Sets the cache to the passed cache.
   * 
   * @param c
   *          the cache to use.
   */
  void setCache( Cache c );

  /**
   * Inserts or updates the given object to the underlying data store. Note that
   * if the filter is null the backend provider might not be able to infer which
   * object should be updated. Make sure the filter uniquely identifies all
   * objects that have to be updated with the passed one.
   * 
   * @param object
   *          the object to update.
   * @param f
   *          the filter that uniquely identifies the object.
   */
  <T extends Model> void update( T object, Filter f );



  Map<String,Object> getResult();


  public <T extends Model> Map<String, Map<String, Long>> getHistograms(List<String> properties, Filter filter, Map<String, List<Integer>> binThresholds)
          throws UnsupportedOperationException;

  <T extends Model> Map<String, Map<String, Long>> getValues(List<String> properties, Filter filter, Map<String, List<Integer>> binThresholds);

  <T extends Model> Map<String, Map<String, Long>> getStats(List<String> properties, Filter filter, Map<String, List<Integer>> binThresholds);
}
