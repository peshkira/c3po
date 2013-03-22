package com.petpet.c3po.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.adaptor.fits.FITSHelper;
import com.petpet.c3po.adaptor.tika.TIKAHelper;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.dao.DBCache;
import com.petpet.c3po.dao.DefaultPersistenceLayer;

/**
 * Configures the application based on a configuration file.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public final class Configurator {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Configurator.class);

  /**
   * The user specified configuration file path. Can be found in ~/.c3poconfig
   */
  public static final String USER_PROPERTIES = System.getProperty("user.home") + File.separator + ".c3poconfig";

  /**
   * The Default persistence layer implementation.
   */
  private DefaultPersistenceLayer persistence;

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

  // TODO change docs when everything is implemented.
  /**
   * Configures the application in the following order: <br>
   * 1. loads the application configuration <br>
   * 2. initializes the persistence layer <br>
   * 3. loads the known properties <br>
   * 4. inits the helper objects.
   */
  public void configure() {
    LOG.debug("Configuring application.");
    this.loadApplicationConfiguration();
    this.initPersistenceLayer();
    this.loadKnownProperties();
    this.initializeHelpers();
  }

  /**
   * Loads the default configuration file and then looks in the users home for a
   * specific configuration. All properties defined there will overwrite the
   * default behavior.
   */
  private void loadApplicationConfiguration() {
    LOG.info("Loading default configuration file");
    this.config = new Properties();

    try {
      this.config.load(Configurator.class.getClassLoader().getResourceAsStream("default.properties"));
    } catch (final IOException e) {
      LOG.error("Default config file not found! {}", e.getMessage());
    }

    LOG.info("Lookig for user defined config file: {}", USER_PROPERTIES);

    final File f = new File(USER_PROPERTIES);

    if (f.exists() && f.isFile()) {
      LOG.debug("Found user defined properties, loading.");
      FileInputStream stream = null;
      try {
        stream = new FileInputStream(f);
        this.config.load(stream);
        LOG.info("User defined config is successfully loaded");
        LOG.debug(this.config.toString());

      } catch (final IOException e) {
        LOG.warn("Could not load user defined properties file '{}', cause: {}", USER_PROPERTIES, e.getMessage());
      } finally {
        try {
          if (stream != null) {
            stream.close();
          }
        } catch (final IOException e) {
          LOG.warn("Could not close the stream in a clean fashion: {}", e.getMessage());
        }
      }

    } else {
      LOG.info("User defined config file was not found.");
    }

  }

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
  public String getStringProperty(final String key) {
    return this.config.getProperty(key, "");
  }

  /**
   * Returns an integer value for the specified property key or -1.
   * 
   * @param key
   *          the key of the property.
   * @return the value or -1.
   */
  public int getIntProperty(final String key) {
    return Integer.parseInt(this.config.getProperty(key, "-1"));
  }

  /**
   * Returns a boolean value for the specified property key or false if none was
   * found.
   * 
   * @param key
   *          the key of the property.
   * @return the value or false.
   */
  public boolean getBooleanProperty(final String key) {
    return Boolean.valueOf(this.config.getProperty(key, "false"));
  }

  /**
   * Initializes an empty cache and the default persistence layer.
   */
  private void initPersistenceLayer() {
    this.persistence = new DefaultPersistenceLayer();

    final DBCache c = new DBCache();
    c.setPersistence(this.persistence);
    
    this.persistence.setCache(c);
    this.persistence.connect(this.config);
  }

  /**
   * Initializes some helper objects.
   */
  private void initializeHelpers() {
    LOG.debug("Initializing helpers.");
    XMLUtils.init();
    FITSHelper.init();
    TIKAHelper.init();
    DataHelper.init();
    // initialize any other helpers...
  }

  private void loadKnownProperties() {
    // TODO
    // load known properties file
    // load properties from db
    // match the properties and update the db if necessary

    // eventually load mapping of properties, e.g. lastModified maps to
    // lastChanged

  }

  /**
   * Static instance holder.
   * 
   * @author Petar Petrov <me@petarpetrov.org>
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
