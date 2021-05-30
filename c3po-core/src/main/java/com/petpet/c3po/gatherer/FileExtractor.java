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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Thanks to: https://gist.github.com/johnkil/4345164
/**
 * Extracts different archive files with apache commons compress.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class FileExtractor {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( FileExtractor.class );

  /**
   * The buffer size used for IO operations.
   */
  private static final int BUFFER_SIZE = 8 * 1024;

  /**
   * Extracts the given archive file to the given destination folder.
   * 
   * @param src
   *          the file to extract.
   * @param dst
   *          the destination folder.
   */
  public static void extract( String src, String dst ) {

    ArchiveInputStream is = getStream( src );
    extract( is, new File( dst ) );

  }

  /**
   * Unpack data from the stream to specified directory.
   * 
   * @param in
   *          stream with tar data
   * @param outputDir
   *          destination directory
   * @return true in case of success, otherwise - false
   */
  private static void extract( ArchiveInputStream in, File outputDir ) {
    try {
      ArchiveEntry entry;
      while ( (entry = in.getNextEntry()) != null ) {
        // replace : for windows OS.
        final File file = new File( outputDir, entry.getName().replaceAll( ":", "_" ) );

        if ( entry.isDirectory() ) {

          if ( !file.exists() ) {
            file.mkdirs();
          }

        } else {
          file.getParentFile().mkdirs();
          BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( file ), BUFFER_SIZE );

          try {

            IOUtils.copy( in, out );
            out.flush();

          } finally {
            try {
              out.close();

            } catch ( IOException e ) {
              LOG.debug( "An error occurred while closing the output stream for file '{}'. Error: {}", file,
                  e.getMessage() );
            }
          }
        }
      }

    } catch ( IOException e ) {
      LOG.debug( "An error occurred while handling archive file. Error: {}", e.getMessage() );
    } finally {
      if ( in != null ) {
        try {
          in.close();
        } catch ( IOException e ) {
          LOG.debug( "An error occurred while closing the archive stream . Error: {}", e.getMessage() );
        }
      }
    }
  }

  /**
   * Obtains an apache compress {@link ArchiveInputStream} to the given archive
   * file.
   * 
   * @param src
   *          the archive file.
   * @return the stream.
   */
  private static ArchiveInputStream getStream( String src ) {
    FileInputStream fis = null;
    ArchiveInputStream is = null;

    try {

      fis = new FileInputStream( src );

      if ( src.endsWith( ".zip" ) ) {

        is = new ZipArchiveInputStream( fis );

      } else {

        boolean zip = src.endsWith( ".tgz" ) || src.endsWith( ".gz" );
        InputStream imp = (zip) ? new GZIPInputStream( fis, BUFFER_SIZE ) : new BufferedInputStream( fis, BUFFER_SIZE );
        is = new TarArchiveInputStream( imp, BUFFER_SIZE );

      }

    } catch ( IOException e ) {
      LOG.warn( "An error occurred while obtaining the stream to the archive '{}'. Error: {}", src, e.getMessage() );
    }
    return is;
  }
}
