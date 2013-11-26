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
package com.petpet.c3po.utils.exceptions;

/**
 * A simple exception to be thrown when a (internal) configuration error
 * occurrs.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class C3POConfigurationException extends Exception {

  /**
   * a generated uid.
   */
  private static final long serialVersionUID = 1961422857090339591L;

  public C3POConfigurationException() {
    super();
  }

  public C3POConfigurationException(final String msg) {
    super( msg );
  }

  public C3POConfigurationException(final String msg, final Throwable cause) {
    super( msg, cause );
  }

  public C3POConfigurationException(final Throwable cause) {
    super( cause );
  }
}
