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
package com.petpet.c3po.api.model;

import java.util.UUID;

/**
 * The source represents a tool that has extracted specific measurements of
 * elements.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class Source implements Model {

  /**
   * The id of the source.
   */
  private String id;

  /**
   * The name of the source.
   */
  private String name;

  /**
   * The version of the source.
   */
  private String version;

  private static int i=0;

  /**
   * A default constructor.
   */
  public Source() {

  }

  /**
   * Creates a new source with the name and the version and auto generates an
   * id.
   * 
   * @param name
   *          the name of the source.
   * @param version
   *          the version of the source.
   */
  public Source(String name, String version) {
    this.id = Integer.toString(i++);
    this.name = name;
    this.version = version;
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion( String version ) {
    this.version = version;
  }
  @Override
  public String toString(){
    return name+":"+version;
  }

}
