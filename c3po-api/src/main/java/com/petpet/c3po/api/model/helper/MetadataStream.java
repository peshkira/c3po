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
package com.petpet.c3po.api.model.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * A simple wrapper around an input stream that holds the fileName (or some
 * human readable identifier) of the stream. Used to output more verbose logs
 * and information to the user.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MetadataStream {

  /**
   * Some human readable name of the digital object that this meta data stream
   * describes.
   */
  private String fileName;

  /**
   * The meta data that was read from the file or source stream.
   */
  private String data;
  
  /**
   * Sets the passed variables.
   * 
   * @param fileName
   *          a human readable identifier of the object that this metadata
   *          stream describes.
   * @param data
   *          the actial meta data in string form.
   */
  public MetadataStream(String fileName, String data) {
    this.fileName = fileName;
    this.data = data;
  }

  public String getFileName() {
    return this.fileName;
  }

  public String getData() {
    return this.data;
  }
}
