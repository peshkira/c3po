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
