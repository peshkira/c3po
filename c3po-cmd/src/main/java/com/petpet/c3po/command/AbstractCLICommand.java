package com.petpet.c3po.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

public abstract class AbstractCLICommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractCLICommand.class);

  private long time = -1L;

  @Override
  public long getTime() {
    return this.time;
  }

  protected void setTime(long time) {
    this.time = time;
  }

  protected void cleanup() {
    try {
      Configurator.getDefaultConfigurator().getPersistence().close();
    } catch (C3POPersistenceException e) {
      LOG.error(e.getMessage());
    }
  }

}
