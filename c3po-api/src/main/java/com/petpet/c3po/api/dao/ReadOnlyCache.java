package com.petpet.c3po.api.dao;

import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;

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
}
