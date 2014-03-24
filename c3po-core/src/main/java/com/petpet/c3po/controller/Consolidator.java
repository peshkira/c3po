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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.utils.DataHelper;

/**
 * The consolidator is a class (worker thread) that processes parsed elements
 * and stores them to the data base. It consolidates the elements meta data if
 * the elements already exist.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 *
 */
public class Consolidator implements Runnable {

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
    private final LinkedBlockingQueue<Element> queue;

    /**
     * The persistence layer for storing the elements.
     */
    private final PersistenceLayer persistence;

    private String name;

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
    public Consolidator(PersistenceLayer p, LinkedBlockingQueue<Element> q) {
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
        try {
            Element e = queue.poll(100, TimeUnit.SECONDS);
            process( e );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.running = true;
        while (!(queue.isEmpty() && !isRunning()))
        {
            try {
                Element e = queue.poll(20, TimeUnit.SECONDS);
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
            LOG.debug( "Element {} exists already, consolidating", stored.getUid() );

            if ( iter.hasNext() ) {
                // obviously, there are more than
                // one elements matching the filter
                // so we cannot assume that it already exists
                // and we just store it.
                this.storeElement( element );
                return;
            }

            consolidate( element, stored );
            this.persistence.update( stored, f );

        } else {
            this.storeElement( element );
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

            this.storeProperties( stored ); // TODO remove in version 0.6
        } catch ( Exception e ) {
            LOG.warn( "An error occurred: {}", e.getMessage() );
        }

    }

    /**
     * Inserts the given element into the db.
     *
     * @param element
     *          the element to insert.
     */
    private void storeElement( Element element ) {
        this.storeProperties( element ); // TODO remove in version 0.6
        this.persistence.insert( element );
    }

    /*
     * TODO remove this method in version 0.6 It is here to just make sure that
     * adaptors that created their own properties and sources , not via the get
     * property and get source methods are still stored.
     */
    private void storeProperties( Element element ) {
        Cache cache = this.persistence.getCache();
        for ( MetadataRecord mr : element.getMetadata() ) {
            Property p = mr.getProperty();
            PropertyType type = PropertyType.valueOf( p.getType() );
            cache.getProperty( p.getKey(), type ); // make sure it is stored

        }

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
