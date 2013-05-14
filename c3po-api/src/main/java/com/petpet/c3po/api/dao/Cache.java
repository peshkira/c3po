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
