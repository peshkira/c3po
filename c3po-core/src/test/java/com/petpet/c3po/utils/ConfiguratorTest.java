package com.petpet.c3po.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;

public class ConfiguratorTest {

  private static final int BUFFER_SIZE = 1024;

  @Test
  public void shouldLoadDefaultConfiguration() throws Exception {
    this.backupRealUserConfigFile();
    
    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();
    
    final PersistenceLayer persistence = configurator.getPersistence();
    Assert.assertTrue(persistence.isConnected());
    Assert.assertEquals(persistence.getDB().getName(), "c3po");
    
    Assert.assertEquals(8, configurator.getIntProperty(Constants.CNF_THREAD_COUNT));
    
    this.restoreUserConfigFile();
  }
  
  @Test
  public void shouldTestUserConfig() throws Exception {
    //first make a backup of a potential real user config
    this.backupRealUserConfigFile();
    //then copy nondefaultconfig to user home
    this.copyTestConfigFile();
    // then test
    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();
    
    final PersistenceLayer persistence = configurator.getPersistence();
    Assert.assertTrue(persistence.isConnected());
    Assert.assertEquals(persistence.getDB().getName(), "test");
    
    Assert.assertEquals(42, configurator.getIntProperty(Constants.CNF_THREAD_COUNT));
    
    // then delete userconfig file
    // then restore the old user config file
    this.restoreUserConfigFile();
  }

  private void backupRealUserConfigFile() {
    File conf = new File(Configurator.USER_PROPERTIES);
    if (conf.exists() && conf.isFile()) {
      conf.renameTo(new File(Configurator.USER_PROPERTIES + ".bak"));
    }
  }
  
  private void copyTestConfigFile() {
    InputStream inStream = null;
    OutputStream outStream = null;

    try {

      final File source = new File("src/test/resources/nondefaultconfig");
      final File target = new File(Configurator.USER_PROPERTIES);

      inStream = new FileInputStream(source);
      outStream = new FileOutputStream(target);

      final byte[] buffer = new byte[BUFFER_SIZE];

      int length;
      // copy the file content in bytes
      while ((length = inStream.read(buffer)) > 0) {
        outStream.write(buffer, 0, length);
      }
      inStream.close();
      outStream.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
  
  private void restoreUserConfigFile() {
    File old = new File(Configurator.USER_PROPERTIES);
    old.delete();
    
    File bak = new File(Configurator.USER_PROPERTIES + ".bak");
    bak.renameTo(new File(Configurator.USER_PROPERTIES));
    
  }

}
