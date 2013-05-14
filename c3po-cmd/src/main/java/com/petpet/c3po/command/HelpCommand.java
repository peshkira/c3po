package com.petpet.c3po.command;

import java.util.Map;

import com.beust.jcommander.JCommander;
import com.petpet.c3po.parameters.Params;

/**
 * Prints help messages for all supported modes of C3PO.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class HelpCommand implements Command {

  /**
   * The params to print.
   */
  private Map<String, Params> params;

  /**
   * Creates the help command.
   * 
   * @param params
   */
  public HelpCommand(Map<String, Params> params) {
    this.params = params;
  }

  /**
   * Prints the help.
   */
  @Override
  public void execute() {
    for ( String mode : params.keySet() ) {
      if ( !mode.equals( "help" ) ) {
        JCommander jc = new JCommander( params.get( mode ) );
        jc.setProgramName( "c3po " + mode );
        jc.usage();
      }
    }
  }

  @Override
  public long getTime() {
    return -1L;
  }

  @Override
  public void setParams( Params params ) {

  }

}
