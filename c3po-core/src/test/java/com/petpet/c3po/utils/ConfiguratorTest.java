package com.petpet.c3po.utils;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;

public class ConfiguratorTest {
  
  private static final Logger LOG = LoggerFactory.getLogger(ConfiguratorTest.class);
  
  private TestConfigGenerator helper;

  @Before
  public void setup() {
    this.helper = new TestConfigGenerator();
    this.helper.backupRealUserConfigFile();
  }
  
  @After
  public void tearDown() {
    this.helper.restoreUserConfigFile();
  }

  @Test
  public void shouldLoadConfiguration() throws Exception {
    LOG.info("Starting test 'shouldLoadConfiguration'");

    this.helper.copyTestConfigFile();
    
    Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();
    
    PersistenceLayer persistence = configurator.getPersistence();
    
    Assert.assertNotNull(persistence);
    Assert.assertEquals(42, configurator.getIntProperty(Constants.CNF_ADAPTORS_COUNT));
    
    if (!persistence.isConnected()) {
      LOG.warn("No connection to the persistence layer was established!");
    }
  }
  
}
