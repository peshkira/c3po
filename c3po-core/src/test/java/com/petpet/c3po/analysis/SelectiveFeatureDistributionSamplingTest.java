package com.petpet.c3po.analysis;

import com.petpet.c3po.adaptor.fits.FITSAdaptor;
import com.petpet.c3po.adaptor.fits.FITSHelper;
import com.petpet.c3po.adaptor.rules.AssignCollectionToElementRule;
import com.petpet.c3po.adaptor.rules.CreateElementIdentifierRule;
import com.petpet.c3po.adaptor.rules.EmptyValueProcessingRule;
import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.adaptor.ProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.dao.MongoPersistenceLayerTest;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.gatherer.LocalFileGatherer;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;
import com.petpet.c3po.utils.XMLUtils;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

/**
 * Created by artur on 17/08/16.
 */
public class SelectiveFeatureDistributionSamplingTest {
    MongoPersistenceLayer pLayer;
    final Logger LOG = LoggerFactory.getLogger(MongoPersistenceLayerTest.class);
    Map<String, Class<? extends ProcessingRule>> knownRules;
    Map<String, Class<? extends AbstractAdaptor>> knownAdaptors;

    @Before
    public void setUp() throws Exception {
        pLayer = new MongoPersistenceLayer();

        Map<String, String> config = new HashMap<String, String>();
        config.put("db.host", "localhost");
        config.put("db.port", "27017");
        config.put("db.name", "c3po_test_db");

        config.put(Constants.OPT_COLLECTION_NAME, "test");
        config.put(Constants.OPT_COLLECTION_LOCATION, "src/test/resources/fits/");
        config.put(Constants.OPT_INPUT_TYPE, "FITS");
        config.put(Constants.OPT_RECURSIVE, "True");
        Map<String, String> adaptorcnf = this.getAdaptorConfig( config, "FITS" );
        DataHelper.init();
        XMLUtils.init();
        FITSHelper.init();
        knownAdaptors = new HashMap<String, Class<? extends AbstractAdaptor>>();
        knownAdaptors.put( "FITS", FITSAdaptor.class );


        DataHelper.init();

        try {
            pLayer.establishConnection(config);
            Configurator.getDefaultConfigurator().setPersistence(pLayer);
        } catch (C3POPersistenceException e) {
            LOG.warn("Could not establish a connection to the persistence layer. All tests will be skipped");
        }


        AbstractAdaptor adaptor=new FITSAdaptor();

        LocalFileGatherer lfg=new LocalFileGatherer(config);
        LinkedBlockingQueue<Element> q=new LinkedBlockingQueue<Element>(10000);


        knownRules = new HashMap<String, Class<? extends ProcessingRule>>();


        knownRules.put( Constants.CNF_ELEMENT_IDENTIFIER_RULE, CreateElementIdentifierRule.class );
        knownRules.put( Constants.CNF_EMPTY_VALUE_RULE, EmptyValueProcessingRule.class );


        LOG.debug( "Initializing helpers." );


        // knownRules.put( Constants.CNF_VERSION_RESOLUTION_RULE, FormatVersionResolutionRule.class );
        // knownRules.put( Constants.CNF_HTML_INFO_RULE, HtmlInfoProcessingRule.class );
        // knownRules.put( Constants.CNF_INFER_DATE_RULE, InferDateFromFileNameRule.class );

        adaptor.setConfig(adaptorcnf);

        List<ProcessingRule> rules = this.getRules( "test");
        lfg.run();
        adaptor.setGatherer(lfg);
        adaptor.setQueue(q);
        adaptor.configure();
        adaptor.setRules( rules );
        adaptor.setCache(pLayer.getCache());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(adaptor);
        executor.shutdown();
        Thread.sleep(500);
        //q.poll(10, TimeUnit.SECONDS);
        while(!q.isEmpty()) {
            pLayer.insert(q.poll());
        }

    }
    private Map<String, String> getAdaptorConfig( Map<String, String> config, String prefix ) {
        final Map<String, String> adaptorcnf = new HashMap<String, String>();
        for ( String key : config.keySet() ) {
            if ( key.startsWith( "c3po.adaptor." ) || key.startsWith( "c3po.adaptor." + prefix.toLowerCase() ) ) {
                adaptorcnf.put( key, config.get( key ) );
            }
        }

        return adaptorcnf;
    }

    private List<ProcessingRule> getRules( String name ) {
        List<ProcessingRule> rules = new ArrayList<ProcessingRule>();
        rules.add( new AssignCollectionToElementRule( name ) ); // always on...

        for ( String key : Constants.RULE_KEYS ) {



            if ( true ) {

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

    @After
    public void tearDown() throws Exception {
        if (this.pLayer.isConnected()) {
            this.pLayer.clearCache();
            this.pLayer.remove(Element.class, null);
            this.pLayer.remove(Property.class, null);
            this.pLayer.remove(Source.class, null);
            try {
                this.pLayer.close();
            } catch (C3POPersistenceException e) {
                LOG.warn("Could not close the connection in a clear fashion");
            }
        }

    }

    @Test
    public void execute() throws Exception {

        SelectiveFeatureDistributionSampling sfd=new SelectiveFeatureDistributionSampling();


        List<String> props = new ArrayList<String>();
        props.add("mimetype");
        props.add("puid");
        props.add("created");

        String proportion="linear";

        Map<String, Object> samplesOptions = new HashMap<String, Object>();
        samplesOptions.put("properties", props );
        samplesOptions.put("pcoverage", "1" );
        samplesOptions.put("tcoverage", "1");
        samplesOptions.put("proportion", proportion );
        samplesOptions.put("threshold", "1000" );
        sfd.setOptions(samplesOptions);
        sfd.readOptions();
        sfd.setFilter(null);
        sfd.execute();

    }

}