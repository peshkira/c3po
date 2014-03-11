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

import com.petpet.c3po.adaptor.browsershot.BrowserShotAdaptor;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.adaptor.fits.FITSAdaptor;
import com.petpet.c3po.adaptor.rules.AssignCollectionToElementRule;
import com.petpet.c3po.adaptor.rules.BrowsershotDissimilarityCountRule;
import com.petpet.c3po.adaptor.rules.CreateElementIdentifierRule;
import com.petpet.c3po.adaptor.rules.DroolsConflictResolutionProcessingRule;
import com.petpet.c3po.adaptor.rules.EmptyValueProcessingRule;
import com.petpet.c3po.adaptor.rules.FormatVersionResolutionRule;
import com.petpet.c3po.adaptor.rules.HtmlInfoProcessingRule;
import com.petpet.c3po.adaptor.rules.InferDateFromFileNameRule;
import com.petpet.c3po.adaptor.tika.TIKAAdaptor;
import com.petpet.c3po.adaptor.browsershot.BrowserShotAdaptor;
import com.petpet.c3po.analysis.CSVGenerator;
import com.petpet.c3po.analysis.ProfileGenerator;
import com.petpet.c3po.analysis.RepresentativeAlgorithmFactory;
import com.petpet.c3po.analysis.RepresentativeGenerator;
import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.adaptor.ProcessingRule;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.gatherer.MetaDataGatherer;
import com.petpet.c3po.api.model.ActionLog;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.gatherer.LocalFileGatherer;
import com.petpet.c3po.utils.ActionLogHelper;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POConfigurationException;

/**
 * A controller that manages the operations coming as input from the client
 * applications. This class ties up the gathering, adaptation and consolidation
 * of data. It acts as a facade of the core to the client applications.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 *
 */
public class Controller {

    /**
     * A default logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger( Controller.class );

    /**
     * The persistence layer that this class uses.
     */
    private PersistenceLayer persistence;

    /**
     * A thread pool for the adaptors.
     */
    private ExecutorService adaptorPool;

    /**
     * A thread pool for the consolidators.
     */
    private ExecutorService consolidatorPool;

    /**
     * A meta data gatherer that collects meta data objects.
     */
    private MetaDataGatherer gatherer;

    /**
     * A processing queue that is passed to each adaptor and is used for the
     * synchronisation between adaptors and consolidators.
     */
    private final Queue<Element> processingQueue;

    /**
     * A map of the known adaptors.
     */
    private final Map<String, Class<? extends AbstractAdaptor>> knownAdaptors;

    /**
     * A map of processing rules.
     */
    private final Map<String, Class<? extends ProcessingRule>> knownRules;

    /**
     * A {@link Configurator} that holds applications specific configurations.
     */
    private Configurator configurator;

    /**
     * A lock object for synchronization between the gatherer and the adaptors.
     */
    private Object gatherLock;

    /**
     * This constructors sets the persistence layer, initializes the processing
     * queue and the {@link LocalFileGatherer};
     *
     * @param config
     *          a configurator that holds application specific configs and can
     *          initialize the application.
     *
     */
    public Controller(Configurator config) {
        this.configurator = config;
        this.persistence = config.getPersistence();
        this.processingQueue = new LinkedList<Element>();
        this.gatherLock = new Object();
        this.gatherer = new LocalFileGatherer( this.gatherLock );
        this.knownAdaptors = new HashMap<String, Class<? extends AbstractAdaptor>>();
        this.knownRules = new HashMap<String, Class<? extends ProcessingRule>>();

        // TODO detect adaptors automatically from the class path
        // and add them to this map.
        this.knownAdaptors.put( "FITS", FITSAdaptor.class );
        this.knownAdaptors.put( "TIKA", TIKAAdaptor.class );
        this.knownAdaptors.put("BrowserShot", BrowserShotAdaptor.class);

        // TODO detect these automatically from the class path
        // and add them to this map.
        // TODO the InferDateFromFileNameRule needs a setter for the cache.
        // TODO - answer by Peter: The cache can be retrieved statically from the Configurator!
        this.knownRules.put( Constants.CNF_ELEMENT_IDENTIFIER_RULE, CreateElementIdentifierRule.class );
        this.knownRules.put( Constants.CNF_EMPTY_VALUE_RULE, EmptyValueProcessingRule.class );
        this.knownRules.put( Constants.CNF_VERSION_RESOLUTION_RULE, FormatVersionResolutionRule.class );
        this.knownRules.put( Constants.CNF_HTML_INFO_RULE, HtmlInfoProcessingRule.class );
        this.knownRules.put( Constants.CNF_INFER_DATE_RULE, InferDateFromFileNameRule.class );
        this.knownRules.put(Constants.CNF_BROWSERSHOT_DISSIMILARITY_COUNT_RULE, BrowsershotDissimilarityCountRule.class);
        this.knownRules.put( Constants.CNF_DROOLS_CONFLICT_RESOLUTION_RULE, DroolsConflictResolutionProcessingRule.class );
    }

    /**
     * This starts a gather-adapt-persist workflow, where all the needed
     * components are configured and run. If the passed options are invalid an
     * exception is thrown. The expected options are: <br>
     *
     * {@link Constants#CNF_CONSOLIDATORS_COUNT} - default 2 <br>
     * {@link Constants#CNF_ADAPTORS_COUNT} - default 4 <br>
     * {@link Constants#OPT_COLLECTION_NAME} <br>
     * {@link Constants#OPT_COLLECTION_LOCATION} <br>
     * {@link Constants#OPT_INPUT_TYPE} <br>
     * and all other options that an adaptor might need. The adaptor will receive
     * only options starting with c3po.adaptor and c3po.adaptor.[prefix]
     *
     * @param options
     *          a map of the application options.
     * @throws C3POConfigurationException
     *           if the configuration is missing or invalid.
     */
    public void processMetaData( Map<String, String> options ) throws C3POConfigurationException {

        this.checkGatherOptions( options );
        this.gatherer.setConfig( options );

        String adaptorsCount = null;
        String consCount = null;

        int consThreads = this.configurator.getIntProperty( Constants.CNF_CONSOLIDATORS_COUNT, 2 );
        if ( consThreads <= 0 ) {
            LOG.warn( "The provided consolidators count config '{}' is negative. Using the default.", consCount );
            consThreads = 2;
        }

        int adaptorThreads = this.configurator.getIntProperty( Constants.CNF_ADAPTORS_COUNT, 4 );
        if ( adaptorThreads <= 0 ) {
            LOG.warn( "The provided consolidators count config '{}' is negative. Using the default.", adaptorsCount );
            adaptorThreads = 4;
        }

        String name = options.get( Constants.OPT_COLLECTION_NAME );
        String type = options.get( Constants.OPT_INPUT_TYPE );
        String prefix = this.getAdaptor( type ).getAdaptorPrefix();
        Map<String, String> adaptorcnf = this.getAdaptorConfig( options, prefix );

        this.startWorkers( name, adaptorThreads, consThreads, type, adaptorcnf );

    }

    /**
     * Generates a profile. The options include the following: <br>
     *
     * {@link Constants#OPT_COLLECTION_NAME} <br>
     * {@link Constants#OPT_OUTPUT_LOCATION} <br>
     * {@link Constants#OPT_INCLUDE_ELEMENTS} <br>
     * {@link Constants#OPT_SAMPLING_ALGORITHM} <br>
     * {@link Constants#OPT_SAMPLING_SIZE} <br>
     * {@link Constants#OPT_SAMPLING_PROPERTIES} <br>
     *
     * @param options
     *          the options to use.
     * @throws C3POConfigurationException
     *           if the options are missing or wrong.
     */
    public void profile( Map<String, Object> options ) throws C3POConfigurationException {
        if ( options == null ) {
            throw new C3POConfigurationException( "No config map provided" );
        }

        List<String> props = (List<String>) options.get( Constants.OPT_SAMPLING_PROPERTIES );
        String alg = (String) options.get( Constants.OPT_SAMPLING_ALGORITHM );
        int size = (Integer) options.get( Constants.OPT_SAMPLING_SIZE );
        String name = (String) options.get( Constants.OPT_COLLECTION_NAME );
        String location = (String) options.get( Constants.OPT_OUTPUT_LOCATION );
        boolean include = (Boolean) options.get( Constants.OPT_INCLUDE_ELEMENTS );

        this.checkAlgOptions( alg, props );

        RepresentativeGenerator samplesGen = new RepresentativeAlgorithmFactory().getAlgorithm( alg );
        Map<String, Object> samplesOptions = new HashMap<String, Object>();
        samplesOptions.put( "properties", props );
        samplesGen.setOptions( samplesOptions );

        ProfileGenerator profileGen = new ProfileGenerator( this.persistence, samplesGen );

        final Filter f = new Filter( new FilterCondition( "collection", name ) );

        final Document profile = profileGen.generateProfile( f, size, include );

        profileGen.write( profile, location + File.separator + name + ".xml" );

        ActionLog log = new ActionLog( name, ActionLog.ANALYSIS_ACTION );
        new ActionLogHelper( this.persistence ).recordAction( log );

    }

    /**
     * Finds sample records that are representative. The options include: <br>
     *
     * {@link Constants#OPT_COLLECTION_NAME} <br>
     * {@link Constants#OPT_SAMPLING_SIZE} <br>
     * {@link Constants#OPT_SAMPLING_ALGORITHM} <br>
     * {@link Constants#OPT_SAMPLING_PROPERTIES} <br>
     *
     * @param options
     *          the options to use
     * @return a list of sample identifiers.
     * @throws C3POConfigurationException
     *           if the options are missing or wrong.
     */
    public List<String> findSamples( Map<String, Object> options ) throws C3POConfigurationException {
        if ( options == null ) {
            throw new C3POConfigurationException( "No options provided" );
        }

        List<String> props = (List<String>) options.get( Constants.OPT_SAMPLING_PROPERTIES );
        String alg = (String) options.get( Constants.OPT_SAMPLING_ALGORITHM );
        int size = (Integer) options.get( Constants.OPT_SAMPLING_SIZE );
        String name = (String) options.get( Constants.OPT_COLLECTION_NAME );

        this.checkAlgOptions( alg, props );

        RepresentativeGenerator samplesGen = new RepresentativeAlgorithmFactory().getAlgorithm( alg );
        Map<String, Object> samplesOptions = new HashMap<String, Object>();
        samplesOptions.put( "properties", props );
        samplesGen.setOptions( samplesOptions );
        samplesGen.setFilter( new Filter( new FilterCondition( "collection", name ) ) );

        ActionLog log = new ActionLog( name, ActionLog.ANALYSIS_ACTION );
        new ActionLogHelper( this.persistence ).recordAction( log );

        return samplesGen.execute( size );
    }

    /**
     * Exports the data in a CSV format. The options include the following: <br>
     *
     * {@link Constants#OPT_COLLECTION_NAME} <br>
     * {@link Constants#OPT_OUTPUT_LOCATION} <br>
     *
     * @param options
     *          the options to use
     * @throws C3POConfigurationException
     *           if the options are missing or wrong.
     */
    public void export( Map<String, Object> options ) throws C3POConfigurationException {
        String name = (String) options.get( Constants.OPT_COLLECTION_NAME );
        String location = (String) options.get( Constants.OPT_OUTPUT_LOCATION );

        CSVGenerator generator = new CSVGenerator( this.persistence );

        generator.exportAll( name, location + File.separator + name + ".csv" );

        ActionLog log = new ActionLog( name, ActionLog.ANALYSIS_ACTION );
        new ActionLogHelper( this.persistence ).recordAction( log );
    }

    /**
     * Removes all elements for a given collection. The options include: <br>
     *
     * {@link Constants#OPT_COLLECTION_NAME}
     *
     * @param options
     *          the options to use.
     * @throws C3POConfigurationException
     *           if the options are missing or invalid
     */
    public void removeCollection( Map<String, Object> options ) throws C3POConfigurationException {
        String name = (String) options.get( Constants.OPT_COLLECTION_NAME );

        if ( name == null || name.equals( "" ) ) {
            throw new C3POConfigurationException( "The collection name cannot be empty" );
        }

        this.persistence.remove( Element.class, new Filter( new FilterCondition( "collection", name ) ) );

        ActionLog log = new ActionLog( name, ActionLog.UPDATED_ACTION );
        new ActionLogHelper( this.persistence ).recordAction( log );
    }

    /**
     * Checks the passed options passed to this controller for required values.
     *
     * @param options
     * @throws C3POConfigurationException
     */
    private void checkGatherOptions( final Map<String, String> options ) throws C3POConfigurationException {

        if ( options == null ) {
            throw new C3POConfigurationException( "No config map provided" );
        }

        String inputType = options.get( Constants.OPT_INPUT_TYPE );
        if ( inputType == null || (!inputType.equals( "TIKA" ) && !inputType.equals( "FITS" ) && !inputType.equals( "BrowserShot" )) ) {
            throw new C3POConfigurationException( "No input type specified. Please use one of FITS, TIKA or BrowserShot." );
        }

        String path = options.get( Constants.OPT_COLLECTION_LOCATION );
        if ( path == null ) {
            throw new C3POConfigurationException( "No input file path provided. Please provide a path to the input files." );
        }

        String name = options.get( Constants.OPT_COLLECTION_NAME );
        if ( name == null || name.equals( "" ) ) {
            throw new C3POConfigurationException( "The name of the collection is not set. Please set a name." );
        }
    }

    /**
     * Checks if the algorithm is distsampling and if it has properties defines.
     * If no, then an exception is thrown.
     *
     * @param alg
     *          the algo to check.
     * @param props
     *          the list of properties for the alg.
     * @throws C3POConfigurationException
     *           if the requirements for the distsampling algorithm are not met.
     */
    private void checkAlgOptions( String alg, List<String> props ) throws C3POConfigurationException {
        if ( alg.equals( "distsampling" ) && (props == null || props.size() == 0) ) {
            throw new C3POConfigurationException(
                    "Cannot use 'distsampling' without properties. Please specify at least one property" );
        }
    }

    /**
     * Filters out only adaptor specific configurations. This method returns a map
     * of configs with keys in the form 'c3po.adaptor.[rest]' or
     * 'c3po.adaptor.[prefix].[rest]', where rest is any arbitrary string and
     * prefix is the adaptor prefix returned in
     * {@link AbstractAdaptor#getAdaptorPrefix()}
     *
     * @param config
     *          the config to filter.
     * @param prefix
     *          the prefix to look for.
     * @return a map with the adaptor specific configuration.
     */
    private Map<String, String> getAdaptorConfig( Map<String, String> config, String prefix ) {
        final Map<String, String> adaptorcnf = new HashMap<String, String>();
        for ( String key : config.keySet() ) {
            if ( key.startsWith( "c3po.adaptor." ) || key.startsWith( "c3po.adaptor." + prefix.toLowerCase() ) ) {
                adaptorcnf.put( key, config.get( key ) );
            }
        }

        return adaptorcnf;
    }

    /**
     * Starts all the workers. Including the adaptors, consolidators and gatherer.
     *
     * @param collection
     *          the name of the collection that is processed.
     * @param adaptThreads
     *          the number of adaptor threads in the pool.
     * @param consThreads
     *          the number of consolidator threads in the pool.
     * @param type
     *          the type of the adaptors.
     * @param adaptorcnf
     *          the adaptor configuration.
     */
    private void startWorkers( String collection, int adaptThreads, int consThreads, String type,
                               Map<String, String> adaptorcnf ) {

        this.adaptorPool = Executors.newFixedThreadPool( adaptThreads );
        this.consolidatorPool = Executors.newFixedThreadPool( consThreads );

        List<Consolidator> consolidators = new ArrayList<Consolidator>();

        LOG.debug( "Initializing consolidators..." );
        for ( int i = 0; i < consThreads; i++ ) {
            Consolidator c = new Consolidator( this.persistence, this.processingQueue );
            consolidators.add( c );
            this.consolidatorPool.submit( c );
        }

        // no more consolidators can be added.
        this.consolidatorPool.shutdown();

        List<ProcessingRule> rules = this.getRules( collection );

        LOG.debug( "Initializing adaptors..." );
        for ( int i = 0; i < adaptThreads; i++ ) {
            AbstractAdaptor a = this.getAdaptor( type );

            a.setCache( this.persistence.getCache() );
            a.setQueue( this.processingQueue );
            a.setGatherLock( this.gatherLock );
            a.setGatherer( this.gatherer );
            a.setConfig( adaptorcnf );
            a.setRules( rules );
            a.configure();

            this.adaptorPool.submit( a );
        }

        // no more adaptors can be added.
        this.adaptorPool.shutdown();

        Thread gathererThread = new Thread( this.gatherer, "MetadataGatherer" );
        // gathererThread.setPriority(Thread.NORM_PRIORITY + 1);
        gathererThread.start();

        try {

            // kills the pool and all adaptor workers after a month;
            boolean adaptorsTerminated = this.adaptorPool.awaitTermination( 2678400, TimeUnit.SECONDS );

            if ( adaptorsTerminated ) {
                this.stopConsoldators( consolidators );
                this.consolidatorPool.awaitTermination( 2678400, TimeUnit.SECONDS );

            } else {
                System.out.println( "Oh my, It seems something went wrong. This process took too long" );
                LOG.error( "Time out occurred, process was terminated" );
            }

        } catch ( InterruptedException e ) {
            LOG.error( "An error occurred: {}", e.getMessage() );
        } finally {
            String path = FileUtils.getTempDirectory().getPath() + File.separator + "c3poarchives";
            FileUtils.deleteQuietly( new File( path ) );
        }

        // allow every rule to execute its tasks after job handling is done, like
        // printing statistics or cleaning up
        for (ProcessingRule processingRule : rules) {
            processingRule.onCommandFinished();
        }

        ActionLog log = new ActionLog( collection, ActionLog.UPDATED_ACTION );
        new ActionLogHelper( this.persistence ).recordAction( log );
    }

    /**
     * Sets the running flag of all consolidator workers in the list to false and
     * notifies them on the processing queue.
     *
     * @param consolidators
     *          the consolidators to stop.
     */
    private void stopConsoldators( List<Consolidator> consolidators ) {
        for ( Consolidator c : consolidators ) {
            c.setRunning( false );
        }

        synchronized ( processingQueue ) {
            this.processingQueue.notifyAll();
        }
    }

    /**
     * Obtains a list of {@link ProcessingRule} objects for the adaptors. The list
     * always contains the {@link AssignCollectionToElementRule} object and all
     * other rules depending on their configurations.
     *
     * @param name
     *          the name of the collection that is going to be processed.
     * @return the list of rules.
     */
    private List<ProcessingRule> getRules( String name ) {
        List<ProcessingRule> rules = new ArrayList<ProcessingRule>();
        rules.add( new AssignCollectionToElementRule( name ) ); // always on...

        for ( String key : Constants.RULE_KEYS ) {

            boolean isOn = this.configurator.getBooleanProperty( key );

            if ( isOn ) {

                Class<? extends ProcessingRule> clazz = this.knownRules.get( key );

                if ( clazz != null ) {

                    try {

                        LOG.debug( "Adding rule '{}'", key );

                        ProcessingRule rule = clazz.newInstance();
                        rules.add( rule );

                    } catch ( InstantiationException e ) {
                        LOG.warn( "Could not initialize the processing rule for key '{}'", key );
                    } catch ( IllegalAccessException e ) {
                        LOG.warn( "Could not access the processing rule for key '{}'", key );
                    }

                }
            }
        }

        return rules;
    }

    /**
     * Gets a new adaptor instance based on the type of adaptor. if the type is
     * unknown, then null is returned.
     *
     * @param type
     *          the type of the adaptor.
     * @return the instance of the adaptor.
     */
    private AbstractAdaptor getAdaptor( String type ) {
        AbstractAdaptor adaptor = null;
        Class<? extends AbstractAdaptor> clazz = this.knownAdaptors.get( type );
        if ( clazz != null ) {
            try {

                adaptor = clazz.newInstance();

            } catch ( InstantiationException e ) {
                LOG.error( "An error occurred while instantiating the adaptor: ", e.getMessage() );
            } catch ( IllegalAccessException e ) {
                LOG.error( "An error occurred while instantiating the adaptor: ", e.getMessage() );
            }
        }

        return adaptor;
    }

}
