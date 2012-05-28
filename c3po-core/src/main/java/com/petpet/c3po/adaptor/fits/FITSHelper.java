package com.petpet.c3po.adaptor.fits;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FITSHelper {

  private static Properties FITS_PROPS;

  public static void init() {
    try {
      InputStream in = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("fits_property_mapping.properties");
      FITS_PROPS = new Properties();
      FITS_PROPS.load(in);
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getPropertyKeyByFitsName(String fitsname) {
    final String prop = (String) FITS_PROPS.get(fitsname);
    return (prop == null) ? fitsname : prop;
  }
}
