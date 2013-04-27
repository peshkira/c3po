package com.petpet.c3po.api.dao;

import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;

/**
 * A simple cache that allows easy retrieval of common objects as properties and
 * sources of meta data.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface Cache {

  /**
   * Retrieves the property designated by the given key. Depending on the
   * implementation it might return null or a new property if there was no
   * property with the given key.
   * 
   * @param key
   *          the key to look for.
   * @return the cached property.
   */
  Property getProperty(String key);

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
  Source getSource(String name, String version);

  /**
   * Any other non-persistence layer object that can be cached.
   * 
   * @param key
   *          the key for the cache.
   * @return the object that was cached in memory.
   */
  Object getObject(Object key);

  /**
   * Puts the given value to the given key. This object does not have to be
   * stored to the persistence layer.
   * 
   * @param key
   *          the key of the object
   * @param value
   *          the value to cache.
   */
  void put(Object key, Object value);

  /**
   * Clears the cache.
   */
  void clear();
}
