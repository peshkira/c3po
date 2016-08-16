package com.petpet.c3po.analysis;

import com.petpet.c3po.adaptor.fits.FITSAdaptor;
import com.petpet.c3po.adaptor.fits.FITSHelper;
import com.petpet.c3po.adaptor.rules.AssignCollectionToElementRule;
import com.petpet.c3po.adaptor.rules.CreateElementIdentifierRule;
import com.petpet.c3po.adaptor.rules.EmptyValueProcessingRule;
import com.petpet.c3po.analysis.conflictResolution.Rule;
import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.adaptor.ProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.dao.MongoPersistenceLayerTest;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.gatherer.LocalFileGatherer;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;
import com.petpet.c3po.utils.XMLUtils;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by artur on 31/03/16.
 */
public class ConflictResolutionProcessorTest {
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

    @After
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

    @Test
    public void resolve() throws Exception {
        ConflictResolutionProcessor crp=new ConflictResolutionProcessor();
        Rule rule=new Rule();
        Element element=new Element(null,null);
        Property creating_application_version = pLayer.getCache().getProperty("creating_application_version");
        MetadataRecord mr=new MetadataRecord(creating_application_version.getKey(),"10 (107b)");
        element.getMetadata().add(mr);

        Filter filter=new Filter();
        filter.addFilterCondition(new FilterCondition("creating_application_version", "CONFLICT"));
        filter.addFilterCondition(new FilterCondition("format", "Microsoft Word"));
        rule.setFilter(filter);
        rule.setElement(element);
        List<Rule> rules=new ArrayList<Rule>();
        rules.add(rule);
        crp.setRules(rules);
        long resolve = crp.resolve(null);
        Assert.assertEquals(resolve, 1);
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

}