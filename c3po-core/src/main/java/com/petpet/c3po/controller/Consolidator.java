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
package com.petpet.c3po.controller;

import java.util.Iterator;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.utils.DataHelper;

/**
 * The consolidator is a class (worker thread) that processes parsed elements
 * and stores them to the data base. It consolidates the elements meta data if
 * the elements already exist.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class Consolidator extends Thread {

  /**
   * A default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( Consolidator.class );

  /**
   * A static instance counter.
   */
  private static int instance = 0;

  /**
   * The queue that will be used for the processing.
   */
  private final Queue<Element> queue;

  /**
   * The persistence layer for storing the elements.
   */
  private final PersistenceLayer persistence;

  /**
   * A flag whether or not the consolidator should run in the next run loop.
   */
  private boolean running;

  /**
   * Creates the consolidator worker.
   * 
   * @param p
   *          the persistence layer.
   * @param q
   *          the queue to use.
   */
  public Consolidator(PersistenceLayer p, Queue<Element> q) {
    persistence = p;
    queue = q;
    setName( "Consolidator[" + instance++ + "]" );
  }

  /**
   * Runs as long as the queue is not empty or the running flag is true. If the
   * running flag is true but the queue is empty, then this worker synchronizes
   * and waits on the queue to be notified. Processes the next element in the
   * queue.
   */
  @Override
  public void run() {
    this.running = true;
    while ( this.running || !queue.isEmpty() ) {
      try {

        Element e = null;
        synchronized ( queue ) {

          while ( queue.isEmpty() ) {

            if ( !this.isRunning() ) {
              break;
            }

            queue.wait();
          }

          // LOG.debug("cons queue count: " + queue.size());
          e = queue.poll();

        }
        // should this be here or outside of the sync block
        process( e );

      } catch ( InterruptedException e ) {
        LOG.warn( "An error occurred in {}: {}", getName(), e.getMessage() );
        break;
      }
    }
    LOG.info( getName() + " is stopping" );
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning( boolean running ) {
    this.running = running;
  }

  /**
   * Checks if the given element already exists and if yes merges the given
   * element meta data with the old metadata. If not, it just stores it to the
   * data base.
   * 
   * @param element
   *          the element to process.
   */
  private void process( Element element ) {
    if ( element == null ) {
      LOG.debug( "Cannot consolidate null element" );

      return;
    }

    // this can be abstracted into a consolidation strategy
    // e.g. consolidate based on equal ids oder based on
    // equal uids or something else.
    Filter f = new Filter( new FilterCondition( "uid", element.getUid() ) );
    Iterator<Element> iter = this.persistence.find( Element.class, f );

    if ( iter.hasNext() ) {
      Element stored = iter.next();

      if ( iter.hasNext() ) {
        // obviously, there are more than
        // one elements matching the filter
        // so we cannot assume that it already exists
        // and we just store it.
        this.persistence.insert( element );
        return;
      }

      consolidate( element, stored );
      this.persistence.update( stored, f );

    } else {
      this.persistence.insert( element );
    }

  }

  /**
   * Merges the two elements.
   * 
   * @param element
   *          the new element
   * @param stored
   *          the stored element.
   */
  private void consolidate( Element element, Element stored ) {
    try {

      for ( MetadataRecord newMR : element.getMetadata() ) {
        DataHelper.mergeMetadataRecord( stored, newMR );
      }

    } catch ( Exception e ) {
      LOG.warn( "An error occurred: {}", e.getMessage() );
    }

  }
}
