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
import com.petpet.c3po.command.GatherCommand;
import com.petpet.c3po.parameters.validation.InputTypeValidator;

/**
 * The supported parameters for the {@link GatherCommand}
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class GatherParams implements Params {

  /**
   * The collection under which to store the gathered data - required. Supports
   * '-c' and '--collection'.
   */
  @Parameter( names = { "-c", "--collection" }, description = "The name of the collection", required = true )
  private String collection;

  /**
   * The input directory, where the meta data is stored - required. Supports
   * '-i' and '--inputdir'.
   */
  @Parameter( names = { "-i", "--inputdir" }, description = "The input directory where the meta data is stored", required = true )
  private String location;

  /**
   * If the flag is present, then the gathering process will traverse the input
   * dir recursively. Default is false. Supports '-r' and '--recursive'.
   */
  @Parameter( names = { "-r", "--recursive" }, arity = 0, description = "Whether or not to gather recursively" )
  private boolean recursive = false;

  /**
   * The type of meta data that will be gathered. Default is 'FITS' Supports
   * '-t' and '--type'.
   */
  @Parameter( names = { "-t", "--type" }, arity = 1, validateValueWith = InputTypeValidator.class, description = "Optional parameter to define the meta data type. Use one of 'FITS' or 'TIKA', to select the type of the input files. Default is FITS" )
  private String type = "FITS";

  public String getCollection() {
    return collection;
  }

  public void setCollection( String collection ) {
    this.collection = collection;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation( String location ) {
    this.location = location;
  }

  public boolean isRecursive() {
    return recursive;
  }

  public void setRecursive( boolean recursive ) {
    this.recursive = recursive;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }
}
