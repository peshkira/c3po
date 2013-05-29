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
package com.petpet.c3po.parameters.validation;

import java.util.Arrays;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

/**
 * Checks that the passed meta data input type is supported.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class InputTypeValidator implements IValueValidator<String> {

  /**
   * The supported metadata types.
   */
  private static final String[] SUPPORTED_METADATA = { "FITS", "TIKA" ,"BrowserShot"};

  /**
   * @throws ParameterException
   *           if the passed value is not supported.
   */
  @Override
  public void validate( String name, String value ) throws ParameterException {
    if ( !Arrays.asList( SUPPORTED_METADATA ).contains( value ) ) {
      throw new ParameterException( "Input type '" + value + "' is not supported. Please use one of "
          + Arrays.deepToString( SUPPORTED_METADATA ) );
    }

  }

}
