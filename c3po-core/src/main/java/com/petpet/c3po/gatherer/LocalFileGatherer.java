package com.petpet.c3po.gatherer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.gatherer.MetaDataGatherer;
import com.petpet.c3po.api.model.helper.MetadataStream;
import com.petpet.c3po.common.Constants;

public class LocalFileGatherer implements MetaDataGatherer {

  private static final Logger LOG = LoggerFactory.getLogger( LocalFileGatherer.class );

  private Map<String, String> config;

  private final Queue<MetadataStream> queue;

  private long sum;

  private boolean ready;

  private Object lock;

  public LocalFileGatherer() {
    this.queue = new LinkedList<MetadataStream>();
    this.ready = false;
  }

  public LocalFileGatherer(Map<String, String> config) {
    this();
    this.config = config;
  }

  public LocalFileGatherer(Object lock) {
    this();
    this.lock = lock;
  }

  @Override
  public synchronized void run() {
    String path = this.config.get( Constants.OPT_COLLECTION_LOCATION );
    boolean recursive = Boolean.valueOf( this.config.get( Constants.OPT_RECURSIVE ) );

    this.ready = false;
    this.traverseFiles( new File( path ), recursive, true );
    System.out.println( this.sum + " files were gathered successfully" );
    LOG.info( "{} files were gathered successfully", this.sum );
    this.ready = true;
    synchronized ( lock ) {
      this.lock.notifyAll();

    }
  }

  public MetadataStream getNext() {
    synchronized ( lock ) {
      return queue.poll();
    }
  }

  private String readStream( String name, InputStream data ) {
    String result = null;
    try {
      result = IOUtils.toString( data );
    } catch ( IOException e ) {
      LOG.warn( "An error occurred, while reading the stream for {}: {}", name, e.getMessage() );
    } finally {
      IOUtils.closeQuietly( data );
    }
    return result;
  }

  private void traverseFiles( File file, boolean recursive, boolean firstLevel ) {

    if ( file.isDirectory() && (recursive || firstLevel) ) {

      File[] files = file.listFiles();
      for ( File f : files ) {
        traverseFiles( f, recursive, false );
      }
    } else {
      String filePath = file.getAbsolutePath();

      if ( isArchive( filePath ) ) {

        processArchive( filePath );

      } else {

        processFile( filePath );

      }

      if ( (this.sum % 1000) == 0 ) {
        LOG.info( "traversed: {} files", this.sum );
        synchronized ( lock ) {
          this.lock.notify();

        }
      }

      if ( this.queue.size() > 10000 && this.sum % 1000 == 0 ) {
        synchronized ( lock ) {
          this.lock.notifyAll();
        }
      }

      if ( this.sum % 10000 == 0 ) {
        System.out.println( this.sum + " files were processed" );
      }
    }
  }

  private void traverseArchive( String filePath, FileObject file ) {
    try {
      FileObject[] children = file.getChildren();
      for ( FileObject child : children ) {
        if ( child.getType() == FileType.FOLDER ) {
          this.traverseArchive( filePath, child );

        } else {
          String name = child.getName().toString();
          FileContent fc = child.getContent();
          InputStream zis = fc.getInputStream();
          String data = this.readStream( name, zis );
          submitMetadataResult( new MetadataStream( name, data ) );
        }
      }
    } catch ( FileSystemException e ) {
      LOG.warn( "Could not resolve file: {}", e.getMessage() );
    }

  }

  private String getPrefix( String filePath ) {
    String prefix = filePath.substring( filePath.lastIndexOf( '.' ) + 1 );
    prefix = (prefix.length() > 4) ? "file://" : prefix + "://";

    if ( filePath.endsWith( ".tar.gz" ) ) {
      prefix = "tgz://";
    }

    return prefix;
  }

  private boolean isArchive( String name ) {
    return name.endsWith( ".zip" ) || name.endsWith( ".bzip2" ) || name.endsWith( ".bz2" ) || name.endsWith( ".gzip" )
        || name.endsWith( ".gz" ) || name.endsWith( ".jar" ) || name.endsWith( ".tar" ) || name.endsWith( ".tar.gz" )
        || name.endsWith( ".tgz" ) || name.endsWith( ".pack" ) || name.endsWith( ".xz" );
  }

  private InputStream getInputStream( String filePath ) {
    try {
      return new BufferedInputStream( new FileInputStream( new File( filePath ) ), 8192 );
    } catch ( FileNotFoundException e ) {
      LOG.warn( "File not found: {}. {}", filePath, e.getMessage() );
      return null;
    }
  }

  private void processFile( String filePath ) {
    InputStream is = this.getInputStream( filePath );
    String data = readStream( filePath, is );
    submitMetadataResult( new MetadataStream( filePath, data ) );

  }

  private void processArchive( String filePath ) {
    try {
      FileSystemManager fsManager = VFS.getManager();
      String prefix = this.getPrefix( filePath );
      FileObject archive = fsManager.resolveFile( prefix + filePath );

      traverseArchive( filePath, archive );

    } catch ( FileSystemException e ) {
      LOG.warn( "Could not resolve file: {}", e.getMessage() );
    }
  }

  private void submitMetadataResult( MetadataStream ms ) {
    this.queue.add( ms );
    sum++;
  }

  @Override
  public void setConfig( Map<String, String> config ) {
    this.config = config;
  }

  public boolean hasNext() {
    return !this.queue.isEmpty();
  }

  @Override
  public boolean isReady() {
    return this.ready;
  }
}
