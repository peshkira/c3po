package com.petpet.c3po.utils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.dao.LocalPersistenceLayer;

public final class Configurator {

  private static final Logger LOG = LoggerFactory.getLogger(Configurator.class);

  private PersistenceLayer persistence;

  public static Configurator getDefaultConfigurator() {
    return ConfiguratorHolder.UNIQUE_INSTANCE;
  }

  /**
   * Hidden default constructor.
   */
  private Configurator() {
    
  }

  public void configure(Map<String, String> config) {
    LOG.debug("Configuring application.");
    this.initPersistenceLayer(config);
    this.initializeHelpers();
    this.loadKnownProperties();
    // eventually load mapping of properties, e.g. lastModified maps to
    // lastChanged
    // TODO load properties files and setup preferences
  }
  
  public PersistenceLayer getPersistence() {
    return this.persistence;
  }

  private void initPersistenceLayer(Map<String, String> config) {
    this.persistence = new LocalPersistenceLayer(config);
  }
  
  private void initializeHelpers() {
    LOG.debug("Initializing helpers.");
    XMLUtils.init();
  }

  private void loadKnownProperties() {
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
