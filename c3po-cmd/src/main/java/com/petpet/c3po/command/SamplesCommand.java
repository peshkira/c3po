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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.controller.Controller;
import com.petpet.c3po.parameters.Params;
import com.petpet.c3po.parameters.SamplesParams;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POConfigurationException;

/**
 * Submits a samples generation request to the controller based on the passed
 * parameters.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class SamplesCommand extends AbstractCLICommand implements Command {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( SamplesCommand.class );

  /**
   * The parameters passed on the command line.
   */
  private SamplesParams params;

  @Override
  public void setParams( Params params ) {
    if ( params != null && params instanceof SamplesParams ) {
      this.params = (SamplesParams) params;
    }
  }

  /**
   * Submits a find samples request to the controller with the passed
   * parameters. If there is no output location specified, then the output is
   * written to the console.
   */
  @Override
  public void execute() {
    long start = System.currentTimeMillis();

    Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();

    Map<String, Object> options = new HashMap<String, Object>();
    options.put( Constants.OPT_COLLECTION_NAME, this.params.getCollection() );
    options.put( Constants.OPT_OUTPUT_LOCATION, this.params.getLocation() );
    options.put( Constants.OPT_SAMPLING_ALGORITHM, this.params.getAlgorithm() );
    options.put( Constants.OPT_SAMPLING_SIZE, this.params.getSize() );
    options.put( Constants.OPT_SAMPLING_PROPERTIES, this.params.getProperties() );

    Controller ctrl = new Controller( configurator );
    try {
      List<String> samples = ctrl.findSamples( options );
      if ( samples.size() == 0 ) {
        System.out.println( "Oh, my! I did not find any samples" );

      } else {
        String location = this.params.getLocation();
        if ( location == null ) {
          print( samples );
        } else {
          try {
            File file = new File( location + File.separator + "samples.txt" );

            if ( !file.exists() ) {
              file.getParentFile().mkdirs();
              file.createNewFile();
            }

            BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
            for ( String sample : samples ) {
              writer.append( sample + "\n" );
            }

            writer.flush();
            writer.close();
          } catch ( IOException e ) {
            LOG.warn( "An error occurred: {}. Outputting to stdout", e.getMessage() );
            print( samples );
          }
        }
      }

    } catch ( C3POConfigurationException e ) {
      LOG.error( e.getMessage() );
      return; // still executes finally :)

    } finally {
      cleanup();
    }

    long end = System.currentTimeMillis();
    this.setTime( end - start );
  }

  /**
   * Prints the list of strings (sample record identifiers) to the console.
   * 
   * @param samples
   *          a list of sample identifiers.
   */
  private void print( List<String> samples ) {
    for ( String sample : samples ) {
      System.out.println( sample );
    }
  }

}
