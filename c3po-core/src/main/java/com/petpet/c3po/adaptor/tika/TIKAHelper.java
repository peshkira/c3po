package com.petpet.c3po.adaptor.tika;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TIKAHelper {

  private static Properties TIKA_PROPS;

  public static void init() {
    try {
      InputStream in = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("tika_property_mapping.properties");
      TIKA_PROPS = new Properties();
      TIKA_PROPS.load(in);
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getPropertyKeyByTikaName(String fitsname) {
    final String prop = (String) TIKA_PROPS.get(fitsname);
    return (prop == null) ? fitsname : prop;
  }
}
