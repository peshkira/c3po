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
package com.petpet.c3po.gatherer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.model.helper.MetadataStream;

/**
 * A simple wrapper around an input stream that holds the fileName (or some
 * human readable identifier) of the stream.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class FileMetadataStream implements MetadataStream {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( FileMetadataStream.class );

  /**
   * Some human readable name of the digital object that this meta data stream
   * describes.
   */
  private String fileName;

  /**
   * Sets the passed variables.
   * 
   * @param fileName
   *          a human readable identifier of the object that this metadata
   *          stream describes.
   * @param data
   *          the actial meta data in string form.
   */
  public FileMetadataStream(String fileName) {
    this.fileName = fileName;
  }

  public String getName() {
    return this.fileName;
  }

  public String getData() {
    String data = null;
    try {
      InputStream is = new BufferedInputStream( new FileInputStream( new File( this.fileName ) ), 8192 );
      data = this.readStream( this.fileName, is );
    } catch ( FileNotFoundException e ) {
      LOG.warn( "An error occurred while openning the stream to {}. Error: {}", fileName, e.getMessage() );
    }

    return data;
  }

  /**
   * Reads the given input stream into memory and returns it. The stream is
   * closed.
   * 
   * @param name
   *          the name of the file/object holding the stream.
   * @param data
   *          the input stream to read.
   * @return the string that was read out of the stream.
   */
  private String readStream( String name, InputStream data ) {
    String result = null;
    try {
      result = IOUtils.toString( data );
    } catch ( IOException e ) {
      LOG.warn( "An error occurred, while reading the stream for {}: {}", name, e.getMessage() );
    } catch ( Exception e ) {
      LOG.warn( "An error occurred, while reading the stream for {}: {}", name, e.getMessage() );
    } finally {
      IOUtils.closeQuietly( data );
    }
    return result;
  }

}
