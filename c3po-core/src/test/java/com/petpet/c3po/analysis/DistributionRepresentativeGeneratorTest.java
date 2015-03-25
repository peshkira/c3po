package com.petpet.c3po.analysis;

import com.petpet.c3po.adaptor.fits.FITSAdaptor;
import com.petpet.c3po.adaptor.fits.FITSHelper;
import com.petpet.c3po.adaptor.rules.*;
import com.petpet.c3po.adaptor.tika.TIKAHelper;
import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.adaptor.ProcessingRule;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.ActionLog;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataStream;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.dao.DefaultPersistenceLayer;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.gatherer.LocalFileGatherer;
import com.petpet.c3po.utils.ActionLogHelper;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;
import com.petpet.c3po.utils.XMLUtils;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class DistributionRepresentativeGeneratorTest extends TestCase {

    public void testExecute() throws Exception {


        List<String> props = new ArrayList<String>();
        props.add("mimetype");
        props.add("puid");

        String alg = "distsampling";
        int size = 5;
        String name = "test";



        RepresentativeGenerator samplesGen = new RepresentativeAlgorithmFactory().getAlgorithm( alg );
        Map<String, Object> samplesOptions = new HashMap<String, Object>();
        samplesOptions.put( "properties", props );
        samplesGen.setOptions( samplesOptions );
        samplesGen.setFilter( new Filter( new FilterCondition( "collection", name ) ) );


        List<String> result = samplesGen.execute(size);
        int i=0;


    }
    MongoPersistenceLayer pLayer;
    Map<String, String> config;
    private static final Logger LOG = LoggerFactory.getLogger(DistributionRepresentativeGeneratorTest.class);
    Map<String, Class<? extends ProcessingRule>> knownRules;
    Map<String, Class<? extends AbstractAdaptor>> knownAdaptors;
    public void setUp() throws Exception {

        Configurator.getDefaultConfigurator().configure();

        pLayer = new MongoPersistenceLayer();
        Configurator.getDefaultConfigurator().setPersistence(pLayer);

        config = new HashMap<String, String>();
        config.put("db.host", "localhost");
        config.put("db.port", "27017");
        config.put("db.name", "c3po_test_db");
        config.put(Constants.OPT_COLLECTION_NAME, "test");
        config.put(Constants.OPT_COLLECTION_LOCATION, "src/test/resources/fits/");
        config.put(Constants.OPT_INPUT_TYPE, "FITS");
        Map<String, String> adaptorcnf = this.getAdaptorConfig( config, "FITS" );
        DataHelper.init();
        XMLUtils.init();
        FITSHelper.init();
        knownAdaptors = new HashMap<String, Class<? extends AbstractAdaptor>>();
        knownAdaptors.put( "FITS", FITSAdaptor.class );


        pLayer.establishConnection(config);
        AbstractAdaptor adaptor=new FITSAdaptor();

        LocalFileGatherer lfg=new LocalFileGatherer(config);
        LinkedBlockingQueue<Element> q=new LinkedBlockingQueue<Element>(10000);


        knownRules = new HashMap<String, Class<? extends ProcessingRule>>();


        knownRules.put( Constants.CNF_ELEMENT_IDENTIFIER_RULE, CreateElementIdentifierRule.class );
        knownRules.put( Constants.CNF_EMPTY_VALUE_RULE, EmptyValueProcessingRule.class );


        LOG.debug( "Initializing helpers." );


       // knownRules.put( Constants.CNF_VERSION_RESOLUTION_RULE, FormatVersionResolutionRule.class∂∂∂ );
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

        adaptor.run();
        while(!q.isEmpty()){
            pLayer.insert(q.poll());
        }

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




    private Map<String, String> getAdaptorConfig( Map<String, String> config, String prefix ) {
        final Map<String, String> adaptorcnf = new HashMap<String, String>();
        for ( String key : config.keySet() ) {
            if ( key.startsWith( "c3po.adaptor." ) || key.startsWith( "c3po.adaptor." + prefix.toLowerCase() ) ) {
                adaptorcnf.put( key, config.get( key ) );
            }
        }

        return adaptorcnf;
    }

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
    public void tearDown() throws Exception {
        if (this.pLayer.isConnected()) {
            this.pLayer.clearCache();
            this.pLayer.remove(Element.class, null);
            this.pLayer.remove(Property.class, null);
            try {
                this.pLayer.close();
            } catch (C3POPersistenceException e) {
                LOG.warn("Could not close the connection in a clear fashion");
            }
        }
    }
}