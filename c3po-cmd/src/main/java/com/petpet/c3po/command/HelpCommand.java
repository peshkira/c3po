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
