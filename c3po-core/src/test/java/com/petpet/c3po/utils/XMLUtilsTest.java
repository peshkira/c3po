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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class XMLUtilsTest {

  @Before
  public void setup() {
    XMLUtils.init();
  }

  @Test
  public void shouldPassXMLValidation() throws Exception {
    boolean valid = XMLUtils.validate(new File("src/test/resources/valid.xml"));
    Assert.assertTrue(valid);
  }

  @Test
  public void shouldFailXMLValidation() throws Exception {
    boolean valid = XMLUtils.validate(new File("src/test/resources/invalid.xml"));
    Assert.assertFalse(valid);
  }
}
