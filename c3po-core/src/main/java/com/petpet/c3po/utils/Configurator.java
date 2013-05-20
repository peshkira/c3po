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
package com.petpet.c3po.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.adaptor.fits.FITSHelper;
import com.petpet.c3po.adaptor.tika.TIKAHelper;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.dao.DBCache;
import com.petpet.c3po.dao.DefaultPersistenceLayer;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

/**
 * Configures the application based on a configuration file. The application
 * provides its own configuration file with default configurations. If you want
 * to overwrite them, you have to create a .c3poconfig file in your home
 * directory. All properties defined within that file will overwrite the
 * defaults. All other properties will use the default values.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public final class Configurator {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( Configurator.class );

  /**
   * The user specified configuration file path. Can be found in ~/.c3poconfig
   */
  public static final String USER_PROPERTIES = System.getProperty( "user.home" ) + File.separator + ".c3poconfig";

  /**
   * The Default persistence layer implementation.
   */
  private PersistenceLayer persistence;

  /**
   * A properties object holding the loaded configuration of the application.
   */
  private Properties config;

  /**
   * Obtains the singleton instance of this class.
   * 
   * @return the configurator.
   */
  public static Configurator getDefaultConfigurator() {
    return ConfiguratorHolder.UNIQUE_INSTANCE;
  }

  /**
   * Hidden default constructor.
   */
  private Configurator() {

  }

  /**
   * Configures the application in the following order: <br>
   * 1. loads the application configuration file <br>
   * 2. initializes the persistence layer with respect to the config <br>
   * 3. inits the helper objects.
   */
  public void configure() {
    this.loadApplicationConfiguration();
    this.initPersistenceLayer();
    this.initializeHelpers();
  }

  /**
   * Retrieves the current persistence layer.
   * 
   * @return {@link PersistenceLayer}
   */
  public PersistenceLayer getPersistence() {
    return this.persistence;
  }

  /**
   * Gets a String representation for the property key or an empty string if no
   * property was found.
   * 
   * @param key
   *          the key of the property
   * @return a string with the value or an empty string if none found.
   */
  public String getStringProperty( final String key ) {
    return this.config.getProperty( key, "" );
  }

  /**
   * Returns the string property for the given key, or returns the default value
   * if nothing is defined under the given key.
   * 
   * @param key
   *          the key to look for
   * @param def
   *          the default value, in case the key does not exists.
   * @return the string value corresponding to that key.
   */
  public String getStringProperty( final String key, final String def ) {
    return this.config.getProperty( key, def );
  }

  /**
   * Returns an integer value for the specified property key or -1.
   * 
   * @param key
   *          the key of the property.
   * @return the value or -1.
   */
  public int getIntProperty( final String key ) {
    return getIntProperty( key, -1 );
  }

  /**
   * Returns an integer for the specified property or the default value if the
   * key is not defined in the configs.
   * 
   * @param key
   *          the key to look for.
   * @param def
   *          the default value if the key does not exist.
   * @return the value corresponding to the key or the default value.
   */
  public int getIntProperty( final String key, final int def ) {
    String property = this.config.getProperty( key );
    int result = def;
    if ( property != null && !property.equals( "" ) ) {
      try {
        result = Integer.parseInt( property );
      } catch ( NumberFormatException e ) {
        // nothing to do - return the default.
        LOG.trace( "The provided property {} is not a number, returning the default: {}", key, def );
      }
    }
    return result;
  }

  /**
   * Returns a boolean value for the specified property key or false if none was
   * found.
   * 
   * @param key
   *          the key of the property.
   * @return the value or false.
   */
  public boolean getBooleanProperty( final String key ) {
    return Boolean.valueOf( this.config.getProperty( key, "false" ) );
  }

  /**
   * Loads the default configuration file and then looks in the users home for a
   * specific configuration. All properties defined there will overwrite the
   * default behavior.
   */
  private void loadApplicationConfiguration() {
    LOG.info( "Loading default configuration file" );
    this.config = new Properties();

    try {
      this.config.load( Configurator.class.getClassLoader().getResourceAsStream( "default.properties" ) );
    } catch ( final IOException e ) {
      LOG.error( "Default config file not found! {}", e.getMessage() );
    }

    LOG.info( "Lookig for user defined config file: {}", USER_PROPERTIES );

    final File f = new File( USER_PROPERTIES );

    if ( f.exists() && f.isFile() ) {
      LOG.debug( "Found user defined properties, loading." );
      FileInputStream stream = null;
      try {
        stream = new FileInputStream( f );
        this.config.load( stream );
        LOG.info( "User defined config is successfully loaded" );
        LOG.debug( this.config.toString() );

      } catch ( final IOException e ) {
        LOG.warn( "Could not load user defined properties file '{}', cause: {}", USER_PROPERTIES, e.getMessage() );
      } finally {
        try {
          if ( stream != null ) {
            stream.close();
          }
        } catch ( final IOException e ) {
          LOG.warn( "Could not close the stream in a clean fashion: {}", e.getMessage() );
        }
      }

    } else {
      LOG.info( "User defined config file was not found." );
    }

  }

  /**
   * Initialises the persistence layer based on the property defined within the
   * config file. If the config file does not define a persistence class or has
   * "default" as value, then the default persistence layer class is
   * initialised. If another class is found, then it is instantiated and set as
   * the persistence layer. However, if the instantiation fails for some reason,
   * then the default persistence layer is initialised, which still might be
   * error prone.
   */
  private void initPersistenceLayer() {

    String persistence = this.getStringProperty( Constants.CNF_PERSISTENCE );

    if ( persistence.equals( "" ) || persistence.equals( "default" ) ) {

      initDefaultPersistence();

    } else {

      try {
        Class<PersistenceLayer> persistenceLayerClazz = (Class<PersistenceLayer>) Class.forName( persistence );

        this.persistence = persistenceLayerClazz.newInstance();

        this.initCache();

      } catch ( ClassNotFoundException e ) {
        LOG.error( "Could not find persistence class '{}' on the classpath! Error: {}", persistence, e.getMessage() );
        LOG.warn( "Trying to use the default persistence layer" );
        initDefaultPersistence();

      } catch ( InstantiationException e ) {
        LOG.error( "Could not instantiate persistence layer class '{}'! Error: {}", persistence, e.getMessage() );
        LOG.warn( "Trying to use the default persistence layer" );
        initDefaultPersistence();

      } catch ( IllegalAccessException e ) {
        LOG.error( "Could not access persistence layer class '{}'! Error: {}", persistence, e.getMessage() );
        LOG.warn( "Trying to use the default persistence layer" );
        initDefaultPersistence();

      } catch ( Exception e ) {
        LOG.error( "Could not start persistence layer class '{}'! Error: {}", persistence, e.getMessage() );
        LOG.warn( "Trying to use the default persistence layer" );
        initDefaultPersistence();

      }

    }

    try {

      Map<String, String> persistenceConfigs = this.getPersistenceConfigs();
      this.persistence.establishConnection( persistenceConfigs );

    } catch ( C3POPersistenceException e ) {
      LOG.error( "Could not establish connection to the data store: {}", e.getMessage() );
    }
  }

  /**
   * Inits the default persistence layer and the cache.
   */
  private void initDefaultPersistence() {
    this.persistence = new DefaultPersistenceLayer();

    this.initCache();
  }

  /**
   * Inits the cache.
   */
  private void initCache() {
    DBCache c = new DBCache();
    c.setPersistence( this.persistence );

    this.persistence.setCache( c );
  }

  /**
   * Takes all properties starting with 'db.' and puts them in a map.
   * 
   * @return a map of all properties related to the db.
   */
  private Map<String, String> getPersistenceConfigs() {
    Map<String, String> dbConfig = new HashMap<String, String>();

    Set<Object> keySet = this.config.keySet();
    for ( Object k : keySet ) {
      String key = (String) k;
      if ( key.startsWith( "db." ) ) {
        dbConfig.put( key, this.getStringProperty( key ) );
      }
    }

    return dbConfig;
  }

  /**
   * Initializes some helper objects.
   */
  private void initializeHelpers() {
    LOG.debug( "Initializing helpers." );
    XMLUtils.init();
    FITSHelper.init();
    TIKAHelper.init();
    DataHelper.init();
    // initialize any other helpers...
  }

  /**
   * Static instance holder.
   * 
   */
  private static final class ConfiguratorHolder {

    /**
     * Unique instance.
     */
    public static final Configurator UNIQUE_INSTANCE = new Configurator();

    /**
     * Hidden default constructor.
     */
    private ConfiguratorHolder() {

    }
  }
}
