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


  public void shouldLoadConfiguration() throws Exception {
    LOG.info("Starting test 'shouldLoadConfiguration'");

    this.helper.copyTestConfigFile();

    //configurator.configure();


  }
  
}
