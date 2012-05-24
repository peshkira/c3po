package com.petpet.c3po.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.dao.LocalPersistenceLayer;

public final class Configurator {

  private static final Logger LOG = LoggerFactory.getLogger(Configurator.class);

  public static final String USER_PROPERTIES = System.getProperty("user.home") + File.separator + ".c3poconfig";

  private LocalPersistenceLayer persistence;

  private Properties config;

  public static Configurator getDefaultConfigurator() {
    return ConfiguratorHolder.UNIQUE_INSTANCE;
  }

  /**
   * Hidden default constructor.
   */
  private Configurator() {

  }

  public void configure() {
    LOG.debug("Configuring application.");
    this.loadApplicationConfiguration();
    this.initPersistenceLayer();
    this.loadKnownProperties();
    this.initializeHelpers();
  }

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


  private void initPersistenceLayer() {
    final DBCache c = new DBCache();

    this.persistence = new LocalPersistenceLayer();
    this.persistence.setCache(c);
    this.persistence.connect(this.config);
  }

  private void initializeHelpers() {
    LOG.debug("Initializing helpers.");
    XMLUtils.init();
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
