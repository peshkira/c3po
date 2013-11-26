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
 * A simple exception that is thrown if something goes wrong in the persistence
 * layer.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class C3POPersistenceException extends Exception {

  /**
   * a generated uid.
   */
  private static final long serialVersionUID = -7832181913915584099L;

  /**
   * {@inheritDoc}
   */
  public C3POPersistenceException() {
    super();
  }

  /**
   * {@inheritDoc}
   * 
   * @param msg
   *          the message to supply
   */
  public C3POPersistenceException(String msg) {
    super( msg );
  }

  /**
   * {@inheritDoc}
   * 
   * @param msg
   *          the message to supply
   * @param cause
   *          the cause for this exception.
   */
  public C3POPersistenceException(String msg, Throwable cause) {
    super( msg, cause );
  }

  /**
   * {@inheritDoc}
   * 
   * @param cause
   *          the cause for this exception.
   */
  public C3POPersistenceException(Throwable cause) {
    super( cause );
  }

}
