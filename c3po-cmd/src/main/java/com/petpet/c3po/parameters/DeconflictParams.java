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
package com.petpet.c3po.parameters;

import com.beust.jcommander.Parameter;
import com.petpet.c3po.command.RemoveCommand;
import com.petpet.c3po.parameters.validation.EmptyStringValidator;

/**
 * The supported paremters for the {@link RemoveCommand}.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class DeconflictParams implements Params {

  /**
   * The name of the collection to deconflict - required. <br>
   * Supports '-c' and '--collection'.
   */
  @Parameter( names = { "-c", "--collection" }, validateValueWith = EmptyStringValidator.class, required = true, description = "The name of the collection" )
  private String collection;

  public String getCollection() {
    return collection;
  }

  public void setCollection( String collection ) {
    this.collection = collection;
  }
}
