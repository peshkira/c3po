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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Model;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

/**
 * This default persistence layer just wraps a real implementation and is used
 * by the {@link Configurator} if no persistence is specified. Currently Mongo
 * is the default backend, as it is the only implementation.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class DefaultPersistenceLayer implements PersistenceLayer {

  /**
   * A default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( DefaultPersistenceLayer.class );

  /**
   * The wrapped persistence layer of this class.
   */
  private PersistenceLayer persistence;

  /**
   * The default constructor initialised the default persistence layer.
   * Currently with the MongoDB implementation.
   */
  public DefaultPersistenceLayer() {
    persistence = new MongoPersistenceLayer();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clearCache() {
    this.persistence.clearCache();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    try {
      this.persistence.close();
    } catch ( C3POPersistenceException e ) {
      LOG.error( "An error occurred: {}", e.getMessage() );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> long count( Class<T> clazz, Filter filter ) {
    return this.persistence.count( clazz, filter );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void establishConnection( Map<String, String> config ) throws C3POPersistenceException {
    this.persistence.establishConnection( config );

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> Iterator<T> find( Class<T> clazz, Filter filter ) {
    return this.persistence.find( clazz, filter );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> List<String> distinct( Class<T> clazz, String f, Filter filter ) {
    return this.persistence.distinct( clazz, f, filter );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Cache getCache() {
    return this.persistence.getCache();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NumericStatistics getNumericStatistics( Property p, Filter filter ) throws UnsupportedOperationException {
    return this.persistence.getNumericStatistics( p, filter );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> Map<String, Long> getValueHistogramFor( Property p, Filter filter )
      throws UnsupportedOperationException {

    return this.persistence.getValueHistogramFor( p, filter );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> void insert( T object ) {
    this.persistence.insert( object );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isConnected() {
    return this.persistence.isConnected();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> void remove( T object ) {
    this.persistence.remove( object );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> void remove( Class<T> clazz, Filter filter ) {
    this.persistence.remove( clazz, filter );
  }

  /**
   * {@inheritDoc}
   */
  public void setCache( Cache c ) {
    this.persistence.setCache( c );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> void update( T object, Filter f ) {
    this.persistence.update( object, f );
  }

  @Override
  public Map<String, Object> getResult() {
    return this.persistence.getResult();
  }

}
