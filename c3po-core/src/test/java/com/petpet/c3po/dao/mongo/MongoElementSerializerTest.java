package com.petpet.c3po.dao.mongo;

import com.mongodb.DBObject;
import com.petpet.c3po.adaptor.fits.FITSAdaptor;
import com.petpet.c3po.adaptor.rules.AssignCollectionToElementRule;
import com.petpet.c3po.adaptor.rules.CreateElementIdentifierRule;
import com.petpet.c3po.adaptor.rules.EmptyValueProcessingRule;
import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.adaptor.ProcessingRule;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.dao.DBCache;
import com.petpet.c3po.dao.MongoPersistenceLayerTest;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by artur on 12/08/16.
 */
public class MongoElementSerializerTest {

    private DBCache cache;
    public static MongoPersistenceLayer pLayer = new MongoPersistenceLayer();
    private Iterator cursor;
    static Logger LOG = LoggerFactory.getLogger(MongoPersistenceLayerTest.class);

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
    static Map<String, Class<? extends ProcessingRule>> knownRules;
    @Test
    public void serialize() throws Exception {
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



        Map<String, String> config = new HashMap<String, String>();
        config.put("db.host", "localhost");
        config.put("db.port", "27017");
        config.put("db.name", "c3po_test_db");
        config.put(Constants.OPT_COLLECTION_NAME, "test");
        config.put(Constants.OPT_COLLECTION_LOCATION, "src/test/resources/fits/");
        config.put(Constants.OPT_INPUT_TYPE, "FITS");
        config.put(Constants.OPT_RECURSIVE, "True");



        pLayer.establishConnection(config);
        Configurator.getDefaultConfigurator().setPersistence(pLayer);



        AbstractAdaptor adaptor=new FITSAdaptor();
        adaptor.configure();

        knownRules = new HashMap<String, Class<? extends ProcessingRule>>();


        knownRules.put( Constants.CNF_ELEMENT_IDENTIFIER_RULE, CreateElementIdentifierRule.class );
        knownRules.put( Constants.CNF_EMPTY_VALUE_RULE, EmptyValueProcessingRule.class );

        adaptor.setRules(  getRules( "test") );
        adaptor.setCache(pLayer.getCache());



        Element test = adaptor.parseElement("test", data);
        MongoElementSerializer serializer=new MongoElementSerializer();
        DBObject serialize = serializer.serialize(test);
        String toString = serialize.toString();
    }

}