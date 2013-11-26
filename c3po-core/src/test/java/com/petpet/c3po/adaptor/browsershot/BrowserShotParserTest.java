/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.petpet.c3po.adaptor.browsershot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author artur
 */
public class BrowserShotParserTest {

  public BrowserShotParserTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of parse method, of class BrowserShotParser.
   */
  @Test
  public void testParse() {
    System.out.println("parse");
    File file = new File("src/test/resources/bs/browsershots-test.xml");
    try {
      FileInputStream is = new FileInputStream(file);
      String data = IOUtils.toString(is);
      BrowserShotParser.parse(data);
    } catch (Exception ex) {
      Logger.getLogger(BrowserShotParserTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}