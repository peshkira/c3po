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
