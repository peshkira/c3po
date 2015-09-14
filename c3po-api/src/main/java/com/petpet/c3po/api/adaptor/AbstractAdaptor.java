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
package com.petpet.c3po.api.adaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.ReadOnlyCache;
import com.petpet.c3po.api.gatherer.MetaDataGatherer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataStream;
import com.petpet.c3po.api.model.helper.PropertyType;

/**
 * The abstract adaptor class provides an encapsulation of a meta data adaptor.
 * This class has to be extended by any adaptor that maps any kind of objects
 * meta data to the internal model of C3PO. The implementor has to overwrite the
 * abstract methods and to provide the ability to parse the data coming from an
 * input stream to an {@link Element}.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 *
 */
public abstract class AbstractAdaptor implements Runnable {

    /**
     * A default logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger( AbstractAdaptor.class );

    /**
     * The gatherer that is used to obtain the next meta data stream.
     */
    private MetaDataGatherer gatherer;

    /**
     * The configuration that is passed to this adaptor before invoking it.
     */
    private Map<String, String> config;

    /**
     * The processing rules that are passed to this adaptor before invoking it.
     */
    private List<ProcessingRule> rules;

    /**
     * A read only instance to the applications cache, so that the adaptor can
     * obtain {@link Property} and {@link Source} objects.
     */
    private Cache cache;

    /**
     * An internally managed queue that will store the parsed Elements for further
     * processing by the application.
     */
    private LinkedBlockingQueue<Element> elementsQueue;

    /**
     * An internally managed queue that will store InputStreams for further
     * processing by the application.
     */
    private LinkedBlockingQueue<MetadataStream> metadataStreamQueue;


    /**
     * This method will be called once before the adaptor is started and gives the
     * implementing class a chance to configure itself. The implementor should
     * make use of the getConfig() methods to read the values for the expected
     * configuration keys of the adaptor. The internally managed config map will
     * contain all configuration keys starting with the prefix 'c3po.adaptor' and
     * all configuration keys starting with the prefix 'c3po.adaptor.[prefix]',
     * where [prefix] is the value retunerd by
     * {@link AbstractAdaptor#getAdaptorPrefix()} of this class in lower case.
     *
     */
    public abstract void configure();

    /**
     * The prefix that this adaptor should be associated with. This method will be
     * used in order to determine which configurations should be passed to this
     * adaptor. The return value of this method will be transformed to a lower
     * case string.
     *
     * @return the prefix of this adaptor.
     */


    private boolean ready;
    public abstract String getAdaptorPrefix();

    /**
     * This element is responsible for adapting the data in the object to a
     * {@link Element}. The implementing class should make use of the
     * {@link PreProcessingRule}s provided by the
     * {@link AbstractAdaptor#getPreProcessingRules()} method in cases where the
     * data allows it. Note that the data string will never be null and will
     * contain the contents of each object that was gathered.
     *
     * @param name
     *          the name of the file/object that is read.
     * @param data
     *          the data to adapt.
     * @return the parsed {@link Element} object.
     */
    public abstract Element parseElement( String name, String data );

    /**
     * Sets the cache to the passed {@link Cache} iff it is not null and the
     * current cache is not set yet.
     *
     * @param cache
     *          the cache to set.
     */
    public final void setCache( Cache cache ) {
        if ( cache != null && this.cache == null ) {
            this.cache = cache;
        }
    }

    /**
     * Sets the gatherer to the passed {@link MetaDataGatherer} iff it is not
     * null.
     *
     * @param gatherer
     *          the gatherer to set.
     */
    public final void setGatherer( MetaDataGatherer gatherer ) {
        if ( gatherer != null && gatherer.getQueue() !=null) {
            this.metadataStreamQueue = gatherer.getQueue();
        }
    }

    public boolean isReady(){
        return ready;
    }

    /**
     * Sets the processing rules to the given rules iff they are not null and the
     * current list is not set yet.
     *
     * @param rules
     *          the list of processing rules.
     */
    public final void setRules( List<ProcessingRule> rules ) {
        if ( rules != null && this.rules == null ) {
            this.rules = rules;
        }
    }

    /**
     * Sets the configuration of this adaptor iff the given config is not null and
     * the current config is not set yet..
     *
     * @param cnf
     *          the config to set.
     */
    public final void setConfig( Map<String, String> cnf ) {
        if ( cnf != null && this.config == null ) {
            this.config = cnf;
        }
    }

    /**
     * Sets the elements queue to the given queue, iff it is not null and the
     * current queue is not set yet. Otherwise a call to this method will do
     * nothing.
     *
     * @param q
     *          the queue to use.
     */
    public final void setQueue( LinkedBlockingQueue<Element> q ) {
        if ( q != null && this.elementsQueue == null ) {
            this.elementsQueue = q;
        }
    }

    /**
     * Starts a loop that runs this thread. The thread checks if the gatherer has
     * a next stream to process. If no, then it sleeps until it is notified by the
     * gatherer. If yes, then it gets the next stream and calls the
     * {@link AbstractAdaptor#parseElement(String, String)} method. Once an
     * element is parsed, then all the post processing rules submitted to this
     * adaptor are run on the element. Then the element is submitted for further
     * processing by the next in chain.
     *
     */
    @Override
    public final void run() {
        try {
            process(metadataStreamQueue.poll(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (!(metadataStreamQueue.isEmpty() ))//&& !gatherer.isReady())) //TODO: check this commented gatherer.
        {
            try {
                process(metadataStreamQueue.poll(2, TimeUnit.SECONDS));
            } catch ( Exception e ) {
                LOG.error("Adaptor stopped unexpectedly {}", Thread.currentThread().getName());
                e.printStackTrace();
            }
        }
    }

    private void process(MetadataStream stream) throws InterruptedException {
        String name = null;
        String data = null;
        if ( stream != null ) {
            name = stream.getName();
            data = stream.getData();
        }
        Element element = null;
        try {

            if ( name != null || data != null ) {
                element = parseElement( name, data );
            }

        } catch ( Exception e ) {
            LOG.warn( "An error occurred while parsing, skipping {}: ", stream.getName(), e.getMessage() );
        }

        postProcessElement( element );

        submitElement(element);
    }

  /*
   * TODO remove in version 0.6
   */
    /**
     * <b>Deprecated</b> This method will be removed in version 0.6<br/>
     * Use one of the <i>getProperty</i> or <i>getSource</i> methods to obtain a
     * new object.
     *
     * Gets a read only instance of the application cache. Can be used to obtain
     * {@link Property} and {@link Source} objects.
     *
     * @return the {@link ReadOnlyCache}
     */
    @Deprecated
    protected ReadOnlyCache getCache() {
        return cache;
    }

    /**
     * Retrieves the property with the given key. If the property does not exist a
     * new property with the default property type is created (STRING).
     *
     * @param key
     *          the key to look for.
     * @return the property.
     */
    protected Property getProperty( String key ) {
        return this.cache.getProperty( key );
    }

    /**
     * Retrieves the property with the given key. If the property does not exists
     * a new property with the given key and type is created. If the property
     * exists but the types do not match, it will be returned anyway.
     *
     * @param key
     *          the key of the property.
     * @param type
     *          the type of the property.
     * @return the property witht the given key.
     */
    protected Property getProperty( String key, PropertyType type ) {
        return this.cache.getProperty( key, type );
    }

    /**
     * Retrieves a source with the given name and version.
     *
     * @param name
     *          the name of the source
     * @param version
     *          the version of the source.
     * @return the source.
     */
    protected Source getSource( String name, String version ) {
        return this.cache.getSource( name, version );
    }

    /**
     * Gets a unmodifiable list of {@link PreProcessingRule} objects. They are
     * sorted by priority, where 0 is the lowest and 1000 is the highest.
     *
     * @return the rules.
     */
    protected final List<PreProcessingRule> getPreProcessingRules() {
        List<ProcessingRule> all = this.getRules();
        List<PreProcessingRule> prerules = new ArrayList<PreProcessingRule>();

        for ( ProcessingRule rule : all ) {
            if ( rule instanceof PreProcessingRule ) {
                prerules.add( (PreProcessingRule) rule );
            }
        }

        return Collections.unmodifiableList( prerules );
    }

    /**
     * Gets a string config for the given key. If there is no value associated
     * with that key, then an empty string is returned.
     *
     * @param key
     *          the key to look for.
     * @return the value for the given key, or an empty string if nothing was
     *         associated with it.
     */
    protected String getConfig( String key ) {
        return this.getStringConfig( key, "" );
    }

    /**
     * Gets a string config for the given key. If there is no value associated
     * with that key, then the given defaultValue string is returned.
     *
     * @param key
     *          the key to look for.
     * @param defaultValue
     *          the value to return if nothing is associated with the given key.
     * @return the value for the given key, or the given defaultValue string if
     *         nothing was associated with it.
     */
    protected String getStringConfig( String key, String defaultValue ) {
        String result = null;
        if ( this.config != null ) {
            result = (String) this.config.get( key );
        }

        if ( result == null ) {
            result = defaultValue;
        }

        return result;
    }

    /**
     * Gets a boolean config for the given key. If there is no value associated
     * with that key, then the given defaultValue is returned.
     *
     * @param key
     *          the key to look for.
     * @param defaultValue
     *          the value to return if nothing is associated with the given key.
     * @return the value for the given key, or the given defaultValue if nothing
     *         was associated with it.
     */
    protected Boolean getBooleanConfig( String key, boolean defaultValue ) {
        Boolean result = null;
        if ( this.config != null ) {
            String val = this.config.get( key );
            if ( val != null && !val.equals( "" ) ) {
                result = Boolean.parseBoolean( val );
            }
        }

        if ( result == null ) {
            result = defaultValue;
        }

        return result;
    }

    /**
     * Gets a integer config for the given key. If there is no value associated
     * with that key or if it is not a valid integer, then the given defaultValue
     * int is returned.
     *
     * @param key
     *          the key to look for.
     * @param defaultValue
     *          the value to return if nothing is associated with the given key.
     * @return the value for the given key, or the given defaultValue if nothing
     *         was associated with it.
     */
    protected Integer getIntegerConfig( String key, int defaultValue ) {
        Integer result = null;
        if ( this.config != null ) {
            String val = this.config.get( key );
            if ( val != null && !val.equals( "" ) ) {
                try {
                    result = Integer.parseInt( val );
                } catch ( NumberFormatException e ) {
                    // do nothing
                }
            }
        }

        if ( result == null ) {
            result = defaultValue;
        }

        return result;
    }

    /**
     * Gets an unmodifiable list of {@link ProcessingRule} objects. All rules are
     * sorted by priority, where 0 is lowest and 1000 is highest.
     *
     * @return the list of rules.
     */
    private final List<ProcessingRule> getRules() {
        if ( this.rules != null ) {

            Collections.sort( this.rules, new Comparator<ProcessingRule>() {

                // sorts from descending
                @Override
                public int compare( ProcessingRule r1, ProcessingRule r2 ) {
                    int first = this.fixPriority( r2.getPriority() );
                    int second = this.fixPriority( r1.getPriority() );
                    return new Integer( first ).compareTo( new Integer( second ) );
                }

                private int fixPriority( int prio ) {
                    if ( prio < 0 )
                        return 0;

                    if ( prio > 1000 )
                        return 1000;

                    return prio;
                }

            } );

        }

        return Collections.unmodifiableList( rules );
    }

    /**
     * Gets a unmodifiable list of {@link PostProcessingRule} objects. They are
     * sorted by descending priority, where 1000 is the highest and 0 is the
     * lowest.
     *
     * @return the rules.
     */
    private final List<PostProcessingRule> getPostProcessingRules() {
        List<ProcessingRule> all = this.getRules();
        List<PostProcessingRule> postrules = new ArrayList<PostProcessingRule>();

        for ( ProcessingRule rule : all ) {
            if ( rule instanceof PostProcessingRule ) {
                postrules.add( (PostProcessingRule) rule );
            }
        }

        return Collections.unmodifiableList( postrules );
    }

    /**
     * Gets all post processing rules and applies them to the given element if it
     * is not null.
     *
     * @param element
     *          the element to post process.
     */
    private void postProcessElement( Element element ) {
        if ( element != null ) {
            List<PostProcessingRule> postProcessingRules = getPostProcessingRules();

            for ( PostProcessingRule rule : postProcessingRules ) {
                element = rule.process( element );
            }
        }
    }

    /**
     * Submits the passed element to the processing queue iff it is not null. This
     * method is synchronized on the queue.
     *
     * @param e
     *          the element to submit to the queue.
     */
    private final void submitElement( Element e ) {
        //synchronized ( elementsQueue ) {
        if ( e != null ) {
            try {
                elementsQueue.put(e);
                if (!this.ready)
                {
                    this.ready=true;
                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            //  }
        }
    }
}
