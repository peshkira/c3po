package com.petpet.c3po.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.petpet.c3po.datamodel.Property;

public class FITSHelper {

  private static Properties FITS_PROPS;
  
  static {
      FITS_PROPS = new Properties();
      InputStream in;
      try {
          in = new FileInputStream("src/main/resources/fits_property_mapping.properties");
          FITS_PROPS.load(in);
          in.close();
      } catch (IOException e) {
          e.printStackTrace();
      }
  }
  
  public static Property getPropertyByFitsName(String name) {
    String prop = (String) FITS_PROPS.get(name);
    
    Property p;
    if (prop != null) {
      p = Helper.getPropertyByName(prop);
    } else {
      p = Helper.getPropertyByName(name);
    }
    
    return p;
  }
}
