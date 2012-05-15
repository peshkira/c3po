package com.petpet.c3po.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.PersistenceLayer;

public class Configurator {

  private static final Logger LOG = LoggerFactory.getLogger(Configurator.class);

  private PersistenceLayer persistence;

  public Configurator(PersistenceLayer p) {
    this.persistence = p;
  }

  public void configure() {
    LOG.debug("Configuring application.");
    this.initializeHelpers();
    this.loadKnownProperties();
    // eventually load mapping of properties, e.g. lastModified maps to
    // lastChanged
    // TODO load properties files and setup preferences
  }

  private void initializeHelpers() {
    LOG.debug("Initializing helpers.");
    XMLUtils.init();
  }

  private void loadKnownProperties() {
  }
}
