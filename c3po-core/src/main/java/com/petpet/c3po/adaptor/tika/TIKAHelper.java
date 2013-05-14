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
package com.petpet.c3po.adaptor.tika;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A simple TIKA helper that maps the tika properties to c3po properties.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class TIKAHelper {

  /**
   * The properties file.
   */
  private static Properties TIKA_PROPS;

  /**
   * Loads the property mappings to the {@link Properties} object.
   */
  public static void init() {
    try {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
          "tika_property_mapping.properties" );
      TIKA_PROPS = new Properties();
      TIKA_PROPS.load( in );
      in.close();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  /**
   * For now we support only the specified properties within the file as the
   * TIKA adaptor is still experimental
   * 
   * @param name
   *          the name of the property
   * @return the normalised property key corresponding to TIKAs property name
   */
  public static String getPropertyKeyByTikaName( String name ) {
    final String prop = (String) TIKA_PROPS.get( name );
    return prop;
  }

}
