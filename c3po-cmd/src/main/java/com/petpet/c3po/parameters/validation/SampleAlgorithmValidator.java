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

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;
import com.petpet.c3po.analysis.RepresentativeAlgorithmFactory;

/**
 * Checks wheter the passed value is a supported representative selection
 * algorithm.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class SampleAlgorithmValidator implements IValueValidator<String> {

  /**
   * @throws ParameterException
   *           if the value is not a valid algorithm.
   */
  @Override
  public void validate( String name, String value ) throws ParameterException {
    if ( !RepresentativeAlgorithmFactory.isValidAlgorithm( name ) ) {
      throw new ParameterException( "Algorithm " + name + " is not supported" );
    }
  }

}
