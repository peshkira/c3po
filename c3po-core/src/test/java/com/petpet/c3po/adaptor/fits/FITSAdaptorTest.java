package com.petpet.c3po.adaptor.fits;

import com.petpet.c3po.adaptor.rules.*;
import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.adaptor.ProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
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

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

/**
 * Created by artur on 22/07/16.
 */
public class FITSAdaptorTest {
    public void parseElement() throws Exception {

        String name="012891.doc";
        String data="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<fits xmlns=\"http://hul.harvard.edu/ois/xml/ns/fits/fits_output\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://hul.harvard.edu/ois/xml/ns/fits/fits_output http://hul.harvard.edu/ois/xml/xsd/fits/fits_output.xsd\" version=\"0.6.0\" timestamp=\"12/27/11 8:05 PM\">\n" +
                "  <identification>\n" +
                "    <identity format=\"Microsoft Word\" mimetype=\"application/msword\" toolname=\"FITS\" toolversion=\"0.6.0\">\n" +
                "      <tool toolname=\"file utility\" toolversion=\"5.03\" />\n" +
                "      <tool toolname=\"Exiftool\" toolversion=\"7.74\" />\n" +
                "      <tool toolname=\"Droid\" toolversion=\"3.0\" />\n" +
                "      <tool toolname=\"NLNZ Metadata Extractor\" toolversion=\"3.4GA\" />\n" +
                "      <tool toolname=\"ffident\" toolversion=\"0.2\" />\n" +
                "      <version toolname=\"Droid\" toolversion=\"3.0\">97-2003</version>\n" +
                "      <externalIdentifier toolname=\"Droid\" toolversion=\"3.0\" type=\"puid\">fmt/40</externalIdentifier>\n" +
                "    </identity>\n" +
                "  </identification>\n" +
                "  <fileinfo>\n" +
                "    <lastmodified toolname=\"Exiftool\" toolversion=\"7.74\" status=\"SINGLE_RESULT\">2011:12:27 19:38:48+01:00</lastmodified>\n" +
                "    <created toolname=\"Exiftool\" toolversion=\"7.74\" status=\"CONFLICT\">2004:02:24 14:49:00</created>\n" +
                "    <created toolname=\"NLNZ Metadata Extractor\" toolversion=\"3.4GA\" status=\"CONFLICT\">2004-02-24 15:49:00</created>\n" +
                "    <creatingApplicationName toolname=\"Exiftool\" toolversion=\"7.74\">Microsoft Word 10.0</creatingApplicationName>\n" +
                "    <creatingApplicationVersion toolname=\"Exiftool\" toolversion=\"7.74\" status=\"CONFLICT\">10 (107b)</creatingApplicationVersion>\n" +
                "    <creatingApplicationVersion toolname=\"NLNZ Metadata Extractor\" toolversion=\"3.4GA\" status=\"CONFLICT\">0x4075</creatingApplicationVersion>\n" +
                "    <filepath toolname=\"OIS File Information\" toolversion=\"0.1\" status=\"SINGLE_RESULT\">/home/petrov/taverna/tmp/021/021891.doc</filepath>\n" +
                "    <filename toolname=\"OIS File Information\" toolversion=\"0.1\" status=\"SINGLE_RESULT\">/home/petrov/taverna/tmp/021/021891.doc</filename>\n" +
                "    <size toolname=\"OIS File Information\" toolversion=\"0.1\" status=\"SINGLE_RESULT\">1824768</size>\n" +
                "    <md5checksum toolname=\"OIS File Information\" toolversion=\"0.1\" status=\"SINGLE_RESULT\">7f4998996ccd385e70e860de578e90b1</md5checksum>\n" +
                "    <fslastmodified toolname=\"OIS File Information\" toolversion=\"0.1\" status=\"SINGLE_RESULT\">1325011128000</fslastmodified>\n" +
                "  </fileinfo>\n" +
                "  <filestatus />\n" +
                "  <metadata>\n" +
                "    <document>\n" +
                "      <pageCount toolname=\"NLNZ Metadata Extractor\" toolversion=\"3.4GA\" status=\"SINGLE_RESULT\">1</pageCount>\n" +
                "      <title toolname=\"NLNZ Metadata Extractor\" toolversion=\"3.4GA\" status=\"SINGLE_RESULT\" />\n" +
                "      <author toolname=\"Exiftool\" toolversion=\"7.74\">Hoyte B. Decker, Jr.</author>\n" +
                "      <isRightsManaged toolname=\"Exiftool\" toolversion=\"7.74\" status=\"SINGLE_RESULT\">no</isRightsManaged>\n" +
                "      <isProtected toolname=\"Exiftool\" toolversion=\"7.74\" status=\"SINGLE_RESULT\">no</isProtected>\n" +
                "      <wordCount toolname=\"NLNZ Metadata Extractor\" toolversion=\"3.4GA\" status=\"SINGLE_RESULT\">11616</wordCount>\n" +
                "      <characterCount toolname=\"NLNZ Metadata Extractor\" toolversion=\"3.4GA\" status=\"SINGLE_RESULT\">66214</characterCount>\n" +
                "      <language toolname=\"NLNZ Metadata Extractor\" toolversion=\"3.4GA\" status=\"SINGLE_RESULT\">U.S. English</language>\n" +
                "    </document>\n" +
                "  </metadata>\n" +
                "</fits>\n";


        List<ProcessingRule> rules = getRules( "test" );
            FITSAdaptor a=new FITSAdaptor();
        a.setRules(rules);
        Element element = a.parseElement(name, data);
        List<MetadataRecord> metadata = element.getMetadata();
        Iterator<MetadataRecord> iterator = metadata.iterator();
        while (iterator.hasNext()){
            MetadataRecord next = iterator.next();
            String key = next.getProperty();
            List<String> value = next.getValues();
            List<String> sources = next.getSources();
            String s = sources.get(0);
            return;
            /*String v=value.get(0);
            if (key.equals("pagecount")){
                Source source = configurator.getPersistence().getCache().getSource(s);
                Assert.assertEquals(source.getName(), "NLNZ Metadata Extractor");
            }*/

        }

    }



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