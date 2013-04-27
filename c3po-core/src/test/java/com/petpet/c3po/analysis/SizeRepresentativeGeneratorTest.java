package com.petpet.c3po.analysis;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.TestConfigGenerator;

public class SizeRepresentativeGeneratorTest {

  
  private TestConfigGenerator helper;
  
  @Before
  public void setup() {
    this.helper = new TestConfigGenerator();
    this.helper.backupRealUserConfigFile();
    this.helper.copyTestConfigFile();
  }
  
  @After
  public void tearDown() {
    this.helper.restoreUserConfigFile();
  }

  @Ignore
  @Test
  public void shouldTestGeneration() throws Exception {
    Configurator.getDefaultConfigurator().configure();
    SizeRepresentativeGenerator gen = new SizeRepresentativeGenerator();
    Filter filter  = new Filter("test", null, null);
    gen.setFilter(filter);
    
    List<String> execute = gen.execute();
    Assert.assertNotNull(execute);
    Assert.assertFalse(execute.isEmpty());
  }
  
}
