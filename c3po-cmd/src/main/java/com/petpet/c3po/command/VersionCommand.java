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

import com.petpet.c3po.C3PO;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.parameters.Params;

/**
 * Prints the version information of c3po.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class VersionCommand implements Command {

  @Override
  public void execute() {
    System.out.println( "I am c3po, human content profiling relations!" );
    System.out.println( "c3po-cmd: " + C3PO.VERSION );
    System.out.println( "c3po-core: " + Constants.CORE_VERSION );
    System.out.println( "c3po-api: " + Constants.API_VERSION );

  }

  @Override
  public long getTime() {
    return -1;
  }

  @Override
  public void setParams( Params params ) {}

}
