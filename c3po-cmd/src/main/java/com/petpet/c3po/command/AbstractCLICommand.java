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
package com.petpet.c3po.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

/**
 * An abstract CLI command that has some helper methods for implementing
 * commands.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public abstract class AbstractCLICommand implements Command {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( AbstractCLICommand.class );

  /**
   * The time needed for executing the command. Default is -1 as some commands
   * do not need time tracking. If -1 is returned, then no time will be printed
   * on the console after execution.
   */
  private long time = -1L;

  /**
   * {@inheritDoc}
   */
  @Override
  public long getTime() {
    return this.time;
  }

  protected void setTime( long time ) {
    this.time = time;
  }

  /**
   * Cleans up the database and logs an error if an exception occurs.
   */
  protected void cleanup() {
    try {
      Configurator.getDefaultConfigurator().getPersistence().close();
    } catch ( C3POPersistenceException e ) {
      LOG.error( e.getMessage() );
    }
  }

}
