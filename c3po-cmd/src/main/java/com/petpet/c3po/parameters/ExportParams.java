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
import com.petpet.c3po.command.ExportCommand;

/**
 * The supported parameters for the {@link ExportCommand}
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class ExportParams implements Params {

  /**
   * The name of the collection - required. Supports '-c' and '--collection'.
   */
  @Parameter( names = { "-c", "--collection" }, description = "The name of the collection", required = true )
  private String collection;

  /**
   * The output directory location - optional. The default is the working
   * directory. Supports '-o' and '--outputdir'.
   */
  @Parameter( names = { "-o", "--outputdir" }, description = "The output directory where the profile will be stored" )
  private String location = "";

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
}
