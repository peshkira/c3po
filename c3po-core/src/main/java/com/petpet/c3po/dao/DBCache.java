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
package com.petpet.c3po.dao;

import java.util.*;

import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.utils.DataHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private Map<String, List<String>> valuesCache;

  /**
   * A map of arbitrary objects.
   */
  private Map<Object, Object> misc;

  /**
   * The persistence layer to use.
   */
  private PersistenceLayer persistence;


  private static final Logger LOG = LoggerFactory.getLogger( DBCache.class );

  /**
   * Creates a new cache with synchronized empty maps.
   */
  public DBCache() {
    this.propertyCache = Collections.synchronizedMap( new HashMap<String, Property>() );
    this.sourceCache = Collections.synchronizedMap( new HashMap<String, Source>() );
    this.misc = Collections.synchronizedMap( new HashMap<Object, Object>() );
    this.valuesCache=Collections.synchronizedMap(new HashMap<String, List<String>>());
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
      LOG.debug("Getting a list of known properties.");
      Iterator<Property> result = this.findProperty( key );

      if ( result.hasNext() ) {
        property = result.next();
        this.propertyCache.put( key, property );

        if ( result.hasNext() ) {
          throw new RuntimeException( "More than one properties found for key: " + key );
        }

      } else {
        property = this.putProperty( key, null );

      }
    }

    return property;
  }

  @Override
  public synchronized List<String> getValues(String property) {
    List<String> values = this.valuesCache.get(property);
    if (values==null){
      LOG.debug("Getting a list of known values.");
      Iterator<String> result=this.findValue( property );
      List<String> vals=new ArrayList<String>();
      while (result.hasNext()){
        vals.add(result.next());
      }
      this.valuesCache.put(property,vals);
    }
    return this.valuesCache.get(property);
  }

  private Iterator<String> findValue(String property) {
    List<String> properties=new ArrayList<String>();
    properties.add(property);
    Map<String, List<Integer>> binThresholds=new HashMap<String, List<Integer>>();
    List<Integer> bins=new ArrayList<Integer>();
    bins.add(5);
    bins.add(20);
    bins.add(40);
    bins.add(100);
    bins.add(1000);
    bins.add(10000);
    bins.add(10000000);
    binThresholds.put("size", bins);
    binThresholds.put("wordcount", bins);
    binThresholds.put("pagecount", bins);
    Map<String, Map<String, Long>> histograms = persistence.getHistograms(properties, new Filter(), binThresholds);
    return histograms.get(property).keySet().iterator();

  }


  /**
   * Looks in the cache for a property with the given key. If the property is
   * found in the cache it is retrieved (no matter if the type matches), if it
   * is not found in the cache, the db is queried. Supposedly the property is
   * found in the db, then it is loaded into the cache and it is returned (no
   * matter if the type matches). If no property with the given key is found in
   * the db, then a new property with the given key and type is created, stored
   * into the db, added to the cache and then it is returned.
   * 
   * @param key
   *          the name of the property.
   * @return the property.
   */
  @Override
  public Property getProperty( String key, PropertyType type ) {
    Property property = this.propertyCache.get( key );

    if ( property == null ) {
      LOG.debug("Adding new item to the list of known properties.");
      Iterator<Property> result = this.findProperty( key );

      if ( result.hasNext() ) {
        property = result.next();
        this.propertyCache.put( key, property );

        if ( result.hasNext() ) {
          throw new RuntimeException( "More than one properties found for key: " + key );
        }
      } else {
        property = this.putProperty( key, type );
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
      LOG.debug("Adding new item to the list of known sources.");
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

    @Override
    public Source getSource(String id) {
      Source source = this.sourceCache.get( id );

      if ( source == null ) {
        LOG.debug("Adding new item to the list of known sources.");
        Iterator<Source> result = this.findSource( id );

        if ( result.hasNext() ) {
          source = result.next();
          this.sourceCache.put( source.getName() + ":" + source.getVersion(), source );
          this.sourceCache.put( source.getId(), source );
          if ( result.hasNext() ) {
            throw new RuntimeException( "More than one sources found for name: " + source.getName() + " and version: " + source.getVersion() );
          }

        }
      }

      return source;




/*
        Iterator<Source> result = this.findSource(id);

        if (result.hasNext()) {
            Source source = result.next();
            if ( result.hasNext() ) {
                throw new RuntimeException( "More than one sources found for id: " + id);
            }
            return source;
        }

        return null;*/
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

  /**
   * Looks for the given source within the DB.
   * 
   * @param name
   *          the name to look for.
   * @param version
   *          the version to look for.
   * @return the Sources that matched the query.
   */
  private Iterator<Source> findSource( String name, String version ) {

    FilterCondition fc1 = new FilterCondition( "name", name );
    FilterCondition fc2 = new FilterCondition( "version", version );
    Filter f = new Filter( Arrays.asList( fc1, fc2 ) );

    return this.persistence.find( Source.class, f );

  }

    private Iterator<Source> findSource(String id) {

        FilterCondition fc1 = new FilterCondition( "_id", id);
        Filter f = new Filter(Arrays.asList(fc1));

        return this.persistence.find(Source.class, f);
    }


    /**
   * Looks for the given property with the given key in the db.
   * 
   * @param key
   *          the key to look for.
   * @return the property.
   */
  private Iterator<Property> findProperty( String key ) {

    FilterCondition fc = new FilterCondition( "key", key );
    Filter f = new Filter( fc );

    return this.persistence.find( Property.class, f );

  }

  /**
   * Creates a new source and inserts it within the db and the cache.
   * 
   * @param name
   *          the name of the source
   * @param version
   *          the version of the source.
   * @return the new source.
   */
  private Source createSource( String name, String version ) {
    Source s = new Source( name, version );

    this.persistence.insert( s );
    this.sourceCache.put( name + ":" + version, s );

    return s;
  }

  /**
   * Creates a new property and inserts it within the db and the cache.
   * 
   * @param key
   *          the key of the prop.
   * @param type
   *          the type of the prop.
   * @return the new source.
   */
  private Property putProperty( String key, PropertyType type ) {
    Property p = new Property( key, type );
    p.setType( DataHelper.getPropertyType( key ) );

    this.persistence.insert( p );
    this.propertyCache.put( key, p );

    return p;
  }

}
