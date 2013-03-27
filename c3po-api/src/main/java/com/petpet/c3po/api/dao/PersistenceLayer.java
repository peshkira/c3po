package com.petpet.c3po.api.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.api.model.Model;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

/**
 * The persistence layer interface offers some common methods for interacting
 * with the underlying database
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface PersistenceLayer {

  /**
   * This method will be called upon initialisation of the application. The map
   * will contain all configurations (loaded from the config file) starting with
   * the prefix 'db'. Example: db.host=192.168.0.1
   * 
   * @param config
   *          the configuration
   */
  void establishConnection(Map<Object, Object> config) throws C3POPersistenceException;

  /**
   * This method will be called once before the applications ends running.
   */
  void close() throws C3POPersistenceException;

  /**
   * Whether or not there is an established connection to the database.
   * 
   * @return true if there is a connection, false otherwise.
   */
  boolean isConnected();

  /**
   * Gets a cache object.
   * 
   * @return the cache of the application.
   */
  Cache getCache();

  /**
   * Clears the current cache of the application.
   */
  void clearCache();

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
  <T extends Model> Iterator<T> find(Class<T> clazz, Filter filter);

  /**
   * Inserts the given object to the underlying data store.
   * 
   * @param object
   *          the object to store.
   */
  <T extends Model> void insert(T object);

  /**
   * Inserts or updates the given object to the underlying data store.
   * 
   * @param object
   *          the object to update.
   */
  <T extends Model> void update(T object);

  /**
   * Removes the given object from the underlying data store.
   * 
   * @param object
   *          the object to remove.
   */
  <T extends Model> void remove(T object);

  /**
   * Removes the objects of the given type that match the filter from the
   * underlying data store. If the filter is null, then all objects are removed.
   * 
   * @param clazz
   *          the type of object to remove
   * @param filter
   *          the filter to match. Can be null.
   */
  <T extends Model> void remove(Class<T> clazz, Filter filter);

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
  <T extends Model> long count(Class<T> clazz, Filter filter);

  /**
   * Returns the distinct values for the given property (in string form). If the
   * passed filter is not null, then it is applied on the collection before
   * 
   * @param clazz
   *          the type of the objects to look at.
   * @param p
   *          the property for which the distinct values have to be applied
   * @param filter
   *          the filter that has to be applied on the data before the distinct
   *          operation is executed
   * @return the distinct values for a given property
   */
  <T extends Model> List<String> distinct(Class<T> clazz, Property p, Filter filter);

  /**
   * Returns a property value histogram for the given property and type and
   * respecting the given filter.
   * 
   * @param clazz
   *          the type of the objects to look at.
   * @param p
   *          the property for which the value histogram will be created
   * @param filter
   *          the filter that has to be applied before the operation is
   *          conducted
   * @return a map with the distinct values as keys (strings) and their
   *         occurrences as map-values (integer)
   * 
   * @throws UnsupportedOperationException
   *           if the current persistence layer cannot create such a histogram.
   */
  <T extends Model> Map<String, Integer> getValueHistogramFor(Class<T> clazz, Property p, Filter filter)
      throws UnsupportedOperationException;

  /**
   * Returns statistics for the given numeric property. The values of the
   * property have to have a numeric data type as the computation might/will
   * involve mathematical calculations.
   * 
   * @param clazz
   *          the type of objects to look at
   * @param p
   *          the property for which the statistcs will be created.
   * @param filter
   *          the filter to apply.
   * @return {@link NumericStatistics} object containing statistics like: min,
   *         max, avg, sd, var, etc.
   * @throws UnsupportedOperationException
   *           if the operation is not supported by the implementing backend
   * @throws IllegalArgumentException
   *           if the arguments are not appropriate, e.g. if the datatype of the
   *           property is not of a numeric type.
   */
  <T extends Model> NumericStatistics getNumericStatistics(Class<T> clazz, Property p, Filter filter)
      throws UnsupportedOperationException, IllegalArgumentException;

  /*
   * DEPRECATED METHODS Will be removed after the changes are done...
   */
  @Deprecated
  DB connect(Map<Object, Object> config);

  @Deprecated
  DB getDB();

  @Deprecated
  DBCursor findAll(String collection);

  @Deprecated
  DBCursor find(String collection, DBObject ref);

  @Deprecated
  DBCursor find(String collection, DBObject ref, DBObject keys);

  @Deprecated
  List distinct(String collection, String key);

  @Deprecated
  List distinct(String collection, String key, DBObject query);

  @Deprecated
  void insert(String collection, DBObject data);

  @Deprecated
  long count(String collection);

  @Deprecated
  long count(String collection, DBObject query);

  @Deprecated
  MapReduceOutput mapreduce(String collection, MapReduceCommand cmd);

}
