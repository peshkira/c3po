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
package com.petpet.c3po;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.petpet.c3po.command.*;
import com.petpet.c3po.parameters.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * This is the entry point for the command line interface.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class C3PO {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( C3PO.class );

  /**
   * The version of the command line interface.
   */
  public static final String VERSION = "0.5.0-SNAPSHOT";

  /**
   * A map of the supported commands for this CLI.
   */
  private Map<String, Command> commands;

  /**
   * A map of parameter objects for each CLI.
   */
  private Map<String, Params> params;

  /**
   * An array of CLI modes. These are the higher level features/commands that
   * the application supports. Most of them have then a combination of different
   * parameters.
   */
  private static final String[] MODES = { "help", "version", "gather", "profile", "samples", "export", "remove", "deconflict" };

  /**
   * Creates the CLI and initializes the maps with all commands and parameters.
   */
  public C3PO() {
    params = new HashMap<String, Params>();
    params.put( MODES[0], new Params() {} );
    params.put( MODES[1], new Params() {} );
    params.put( MODES[2], new GatherParams() );
    params.put( MODES[3], new ProfileParams() );
    params.put( MODES[4], new SamplesParams() );
    params.put( MODES[5], new ExportParams() );
    params.put( MODES[6], new RemoveParams() );
    params.put(MODES[7], new DeconflictParams());

    commands = new HashMap<String, Command>();
    commands.put( MODES[0], new HelpCommand( params ) );
    commands.put( MODES[1], new VersionCommand() );
    commands.put( MODES[2], new GatherCommand() );
    commands.put( MODES[3], new ProfileCommand() );
    commands.put( MODES[4], new SamplesCommand() );
    commands.put( MODES[5], new ExportCommand() );
    commands.put( MODES[6], new RemoveCommand() );
    commands.put( MODES[7], new ResolveConflictsCommand() );
  }

  /**
   * Runs a command that is inferred from the given mode and passes all the
   * arguments as parameters.
   * 
   * @param mode
   *          the mode in which the CLI should run
   * @param args
   *          the parameters for the command.
   */
  private void compute( String mode, String[] args ) {

    if ( !Arrays.asList( MODES ).contains( mode ) ) {

      System.err.println( "Oh my, does not compute. Unknown mode: " + mode );
      new HelpCommand( params ).execute();
      System.exit( 1 );

    }

    Params params = this.params.get( mode );
    JCommander jc = new JCommander( params );
    jc.setProgramName( "c3po " + mode );

    try {

      jc.parse( Arrays.copyOfRange( args, 1, args.length ) );

      Command command = this.commands.get( mode );

      if ( command == null ) {
        throw new ParameterException( "Unknown mode '" + mode + "'." );
      }

      System.out.println( "Hello, I am c3po, human content profiling relations" );
      command.setParams( params );
      command.execute();

      long time = command.getTime();

      if ( time != -1 ) {
        System.out.println( "Success. Execution Time: " + time + "ms" );
      }

    } catch ( ParameterException e ) {

      LOG.warn( "{}", e.getMessage() );
      System.err.println( e.getMessage() );
      jc.usage();

    }
  }

  /**
   * The entry point for this command line interfaces. If no arguments are
   * submitted a help message is printed and the program exits.
   * 
   * @param args
   *          the arguments to pass to the CLI.
   */
  public static void main( String[] args ) {

    if ( args.length == 0 ) {
      System.err.println( "Please use one of the following arguments: " + Arrays.deepToString( MODES ) );
      System.exit( 1 );
    }

    C3PO c3po = new C3PO();
    c3po.compute( args[0], args );
  }

}
