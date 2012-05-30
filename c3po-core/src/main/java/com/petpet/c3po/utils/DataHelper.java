package com.petpet.c3po.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataHelper {

  private static final Logger LOG = LoggerFactory.getLogger(DataHelper.class);

  private static Properties TYPES;

  public static void init() {
    try {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("datatypes.properties");
      TYPES = new Properties();
      TYPES.load(in);
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public static String getPropertyType(String key) {
    return TYPES.getProperty(key, "STRING");
  }


  private DataHelper() {

  }
}
