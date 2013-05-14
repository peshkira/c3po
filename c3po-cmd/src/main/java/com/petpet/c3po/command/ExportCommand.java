package com.petpet.c3po.command;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.controller.Controller;
import com.petpet.c3po.parameters.ExportParams;
import com.petpet.c3po.parameters.Params;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POConfigurationException;

/**
 * Submits an export to csv request to the controller based on the passed
 * parameters.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class ExportCommand extends AbstractCLICommand implements Command {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( ExportCommand.class );

  /**
   * The parameters passed on the command line.
   */
  private ExportParams params;

  /**
   * Creates a controller and submits an export request.
   */
  @Override
  public void execute() {
    long start = System.currentTimeMillis();
    LOG.info( "Starting csv export of all data" );

    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();

    Map<String, Object> options = new HashMap<String, Object>();
    options.put( Constants.OPT_COLLECTION_NAME, this.params.getCollection() );
    options.put( Constants.OPT_OUTPUT_LOCATION, this.params.getLocation() );

    Controller ctrl = new Controller( configurator );

    try {
      ctrl.export( options );
    } catch ( C3POConfigurationException e ) {
      LOG.error( e.getMessage() );
      return;

    } finally {
      cleanup();
    }

    long end = System.currentTimeMillis();
    this.setTime( end - start );
  }

  @Override
  public void setParams( Params params ) {
    if ( params != null && params instanceof ExportParams ) {
      this.params = (ExportParams) params;
    }
  }

}
