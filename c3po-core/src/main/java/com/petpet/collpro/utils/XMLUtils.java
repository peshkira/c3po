package com.petpet.collpro.utils;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.datamodel.ValueStatus;

public final class XMLUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(XMLUtils.class);
    
    private static SAXParserFactory factory;
   
    static {
        XMLUtils.factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);
    }
    
    public static boolean validate(File f)  {
        try {
            SAXParser parser = factory.newSAXParser();
            parser.setProperty(Constants.XML_SCHEMA_PROPERTY, Constants.XML_SCHEMA_LANGUAGE);

            SimpleErrorHandler errorHandler = new SimpleErrorHandler();
            
            SAXReader reader = new SAXReader(parser.getXMLReader());
            reader.setValidation(true);
            
            reader.setErrorHandler(errorHandler);
            reader.read(f);
            
            return errorHandler.isValid();
            
        } catch (ParserConfigurationException e) {
            LOG.error("ParserConfigurationException: {}", e.getMessage());
        } catch (SAXException e) {
            LOG.error("SAXException: {}", e.getMessage());
        } catch (DocumentException e) {
            LOG.error("DocumentException: {}", e.getMessage());
        }
        
        return false;
    }
    
    public static ValueStatus getStatusOfFITSElement(org.dom4j.Element elmnt) {
        ValueStatus status = ValueStatus.OK;
        String statAttr = elmnt.attributeValue("status");
        if (statAttr != null && !statAttr.equals("")) {
            status = ValueStatus.valueOf(statAttr);
        }
        
        return status;
    }
    
    private XMLUtils() {}
}
