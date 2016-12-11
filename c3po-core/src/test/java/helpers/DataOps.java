package helpers;

import com.petpet.c3po.adaptor.fits.FITSAdaptor;
import com.petpet.c3po.adaptor.rules.AssignCollectionToElementRule;
import com.petpet.c3po.adaptor.rules.CreateElementIdentifierRule;
import com.petpet.c3po.adaptor.rules.EmptyValueProcessingRule;
import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.adaptor.ProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataStream;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.dao.MongoPersistenceLayerTest;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.gatherer.LocalFileGatherer;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by artur on 31/08/16.
 */
public class DataOps {
    static Logger LOG = LoggerFactory.getLogger(MongoPersistenceLayerTest.class);

    public static MongoPersistenceLayer pLayer = new MongoPersistenceLayer();
    public static boolean runTestsOnRealDataset=false;

    static Map<String, Class<? extends ProcessingRule>> knownRules;
    public static void insertData(){
        Map<String, String> config = new HashMap<String, String>();
        if (runTestsOnRealDataset) {               //REAL DATASET DETAILS!
            config.put("db.host", "localhost");
            config.put("db.port", "27017");
            config.put("db.name", "c3po");
            config.put(Constants.OPT_COLLECTION_NAME, "test");
        }
        else {
            config.put("db.host", "localhost");
            config.put("db.port", "27017");
            config.put("db.name", "c3po_test_db");
            config.put(Constants.OPT_COLLECTION_NAME, "test");
        }
        config.put(Constants.OPT_COLLECTION_LOCATION, "src/test/resources/fits/");
        config.put(Constants.OPT_INPUT_TYPE, "FITS");
        config.put(Constants.OPT_RECURSIVE, "True");


        try {
            pLayer.establishConnection(config);
            Configurator.getDefaultConfigurator().setPersistence(pLayer);
        } catch (C3POPersistenceException e) {
            LOG.warn("Could not establish a connection to the persistence layer. All tests will be skipped");
        }

        AbstractAdaptor adaptor=new FITSAdaptor();
        adaptor.configure();

        knownRules = new HashMap<String, Class<? extends ProcessingRule>>();


        knownRules.put( Constants.CNF_ELEMENT_IDENTIFIER_RULE, CreateElementIdentifierRule.class );
        knownRules.put( Constants.CNF_EMPTY_VALUE_RULE, EmptyValueProcessingRule.class );

        adaptor.setRules(  getRules( "test") );
        adaptor.setCache(pLayer.getCache());
        LocalFileGatherer lfg=new LocalFileGatherer(config);
        lfg.run();
        while (lfg.hasNext()){
            MetadataStream next = lfg.getNext();
            Element element = adaptor.parseElement(next.getName(), next.getData());
            pLayer.insert(element);

        }
    }
    public static void removeData(){
        if (pLayer.isConnected()) {
            pLayer.clearCache();
            pLayer.remove(Element.class, null);
            pLayer.remove(Property.class, null);
            pLayer.remove(Source.class, null);
            try {
                pLayer.close();
            } catch (C3POPersistenceException e) {
                LOG.warn("Could not close the connection in a clear fashion");
            }
        }



    }


    private static List<ProcessingRule> getRules(String name ) {
        List<ProcessingRule> rules = new ArrayList<ProcessingRule>();
        rules.add( new AssignCollectionToElementRule( name ) ); // always on...

        for ( String key : Constants.RULE_KEYS ) {


            if ( true ) {

                Class<? extends ProcessingRule> clazz = knownRules.get( key );

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

}
