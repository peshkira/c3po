package com.petpet.c3po.command;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.controller.Controller;
import com.petpet.c3po.parameters.Params;
import com.petpet.c3po.parameters.ProfileParams;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POConfigurationException;

/**
 * Submits a profile request to the controller based on the passed parameters.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class ProfileCommand extends AbstractCLICommand implements Command {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( ProfileCommand.class );

  /**
   * The profile parameter passed on the command line.
   */
  private ProfileParams params;

  /**
   * Submits a profile request to the controller.
   */
  @Override
  public void execute() {
    final long start = System.currentTimeMillis();

    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();

    Map<String, Object> options = new HashMap<String, Object>();
    options.put( Constants.OPT_COLLECTION_NAME, this.params.getCollection() );
    options.put( Constants.OPT_OUTPUT_LOCATION, this.params.getLocation() );
    options.put( Constants.OPT_SAMPLING_ALGORITHM, this.params.getAlgorithm() );
    options.put( Constants.OPT_SAMPLING_SIZE, this.params.getSize() );
    options.put( Constants.OPT_SAMPLING_PROPERTIES, this.params.getProperties() );
    options.put( Constants.OPT_INCLUDE_ELEMENTS, this.params.isIncludeElements() );

    Controller ctrl = new Controller( configurator );

    try {
      ctrl.profile( options );
    } catch ( C3POConfigurationException e ) {
      LOG.error( e.getMessage() );
      return;

    } finally {
      cleanup();
    }

    final long end = System.currentTimeMillis();
    this.setTime( end - start );
  }

  @Override
  public void setParams( Params params ) {
    if ( params != null && params instanceof ProfileParams ) {
      this.params = (ProfileParams) params;
    }

  }

}
