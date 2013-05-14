package com.petpet.c3po.dao;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.utils.DataHelper;

/**
 * Implements the {@link Cache} interface.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class DBCache implements Cache {

  /**
   * A map of {@link Property} objects.
   */
  private Map<String, Property> propertyCache;

  /**
   * A map of {@link Source} objects.
   */
  private Map<String, Source> sourceCache;

  /**
   * A map of arbitrary objects.
   */
  private Map<Object, Object> misc;

  /**
   * The persistence layer to use.
   */
  private PersistenceLayer persistence;

  /**
   * Creates a new cache with synchronized empty maps.
   */
  public DBCache() {
    this.propertyCache = Collections.synchronizedMap( new HashMap<String, Property>() );
    this.sourceCache = Collections.synchronizedMap( new HashMap<String, Source>() );
    this.misc = Collections.synchronizedMap( new HashMap<Object, Object>() );
  }

  /**
   * Sets the persistence layer
   * 
   * @param persistence
   *          the persitence to use.
   */
  public void setPersistence( PersistenceLayer persistence ) {
    this.persistence = persistence;
  }

  /**
   * Looks in the cache for a property with the given key. If the property is
   * found in the cache it is retrieved, if it is not found in the cache, the db
   * is queried. Supposedly the property is found in the db, then it is loaded
   * into the cache and it is returned. If no property with the given key is
   * found in the db, then a new property is created, stored into the db, added
   * to the cache and then it is returned.
   * 
   * @param key
   *          the name of the property.
   * @return the property.
   */
  @Override
  public synchronized Property getProperty( String key ) {
    Property property = this.propertyCache.get( key );

    if ( property == null ) {
      Iterator<Property> result = this.findProperty( key );

      if ( result.hasNext() ) {
        property = result.next();
        this.propertyCache.put( key, property );

        if ( result.hasNext() ) {
          throw new RuntimeException( "More than one properties found for key: " + key );
        }

      } else {
        property = this.createProperty( key );

      }
    }

    return property;
  }

  /**
   * Looks in the cache for a source with the given name and version. If the
   * source is found in the cache it is retrieved, if it is not found in the
   * cache, the db is queried. Supposedly the source is found in the db, then it
   * is loaded into the cache and it is returned. If no source with the given
   * name and version is found in the db, then a new source is created, stored
   * into the db, added to the cache and then it is returned.
   * 
   * @param name
   *          the name of the source.
   * @param version
   *          the version of the source.
   * @return the source.
   */
  @Override
  public synchronized Source getSource( String name, String version ) {
    Source source = this.sourceCache.get( name + ":" + version );

    if ( source == null ) {
      Iterator<Source> result = this.findSource( name, version );

      if ( result.hasNext() ) {
        source = result.next();
        this.sourceCache.put( name + ":" + version, source );

        if ( result.hasNext() ) {
          throw new RuntimeException( "More than one sources found for name: " + name + " and version: " + version );
        }

      } else {
        source = this.createSource( name, version );

      }
    }

    return source;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getObject( Object key ) {
    return this.misc.get( key );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void put( Object key, Object value ) {
    this.misc.put( key, value );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void clear() {
    this.propertyCache.clear();
    this.sourceCache.clear();
    this.misc.clear();
  }

  private Iterator<Source> findSource( String name, String version ) {

    FilterCondition fc1 = new FilterCondition( "name", name );
    FilterCondition fc2 = new FilterCondition( "version", version );
    Filter f = new Filter( Arrays.asList( fc1, fc2 ) );

    return this.persistence.find( Source.class, f );

  }

  private Iterator<Property> findProperty( String key ) {

    FilterCondition fc = new FilterCondition( "key", key );
    Filter f = new Filter( fc );

    return this.persistence.find( Property.class, f );

  }

  private Source createSource( String name, String version ) {
    Source s = new Source( name, version );

    this.persistence.insert( s );
    this.sourceCache.put( name + ":" + version, s );

    return s;
  }

  private Property createProperty( String key ) {
    Property p = new Property( key );
    p.setType( DataHelper.getPropertyType( key ) );

    this.persistence.insert( p );
    this.propertyCache.put( key, p );

    return p;
  }

}
