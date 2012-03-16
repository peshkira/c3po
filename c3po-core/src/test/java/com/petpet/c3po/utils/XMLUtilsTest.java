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
