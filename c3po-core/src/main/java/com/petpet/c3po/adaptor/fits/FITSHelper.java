package com.petpet.c3po.adaptor.fits;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A simple helper that reads a file of fits to c3po property mappings.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class FITSHelper {

  /**
   * The properties file.
   */
  private static Properties FITS_PROPS;

  /**
   * Reads the mappings into the {@link Properties} object.
   */
  public static void init() {
    try {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
          "fits_property_mapping.properties" );
      FITS_PROPS = new Properties();
      FITS_PROPS.load( in );
      in.close();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the mapping for the given fits property name or the same name if no
   * mapping was defined.
   * 
   * @param fitsname
   *          the fits property name to look for.
   * @return the mapping or the fits name.
   */
  public static String getPropertyKeyByFitsName( String fitsname ) {
    final String prop = (String) FITS_PROPS.get( fitsname );
    return (prop == null) ? fitsname : prop;
  }
}
