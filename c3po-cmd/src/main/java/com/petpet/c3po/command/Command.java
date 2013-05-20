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

import com.petpet.c3po.parameters.Params;

/**
 * A command interface for the CLI. It denotes a mode in which the C3PO CLI can
 * operate.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface Command {

  /**
   * Sets the parameter for the command (that were passed on the command line).
   * 
   * @param params
   *          the params to set.
   */
  void setParams( Params params );

  /**
   * Called if everything was ok and should execute the current command. Note
   * that the {@link Command#setParams(Params)} method will be called exactly
   * once before calling this method. The method should optionally record its
   * time for execution (where needed) and return it in
   * {@link Command#getTime()}.
   */
  void execute();

  /**
   * Retrieves the time needed for the execution of this command in
   * milliseconds. If the command does not require time tracking, then -1 is
   * returned.
   * 
   * @return the time needed for this command to execute.
   */
  long getTime();
}
