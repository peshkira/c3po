package com.petpet.c3po.utils;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;

public class ConfiguratorTest {
  
  private TestConfigGenerator helper;
 private Configurator configurator;
 private PersistenceLayer persistence;
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
  public void shouldLoadDefaultConfiguration() throws Exception {
    configurator = Configurator.getDefaultConfigurator();
    configurator.configure();
    
    persistence = configurator.getPersistence();
    Assert.assertTrue(persistence.isConnected());
    Assert.assertEquals(persistence.getDB().getName(), "c3po");
    Assert.assertEquals(8, configurator.getIntProperty(Constants.CNF_THREAD_COUNT));
  }
  
  @Test
  public void shouldTestUserConfig() throws Exception {
    //first make a backup of a potential real user config
    //then copy nondefaultconfig to user home
    // see setup() method 
    
    this.helper.copyTestConfigFile();
    // then test
    configurator = Configurator.getDefaultConfigurator();
    configurator.configure();
    
    persistence = configurator.getPersistence();
    Assert.assertTrue(persistence.isConnected());
    Assert.assertEquals(persistence.getDB().getName(), "test");
    
    Assert.assertEquals(42, configurator.getIntProperty(Constants.CNF_THREAD_COUNT));
    // then delete userconfig file
    // then restore the old user config file
    // see tearDown() method
  }


  
 

}
