package com.petpet.collpro.tools;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class FITSMetaDataConverterTest {
    
    private FITSMetaDataConverter converter;
    
    @Before
    public void before() {
        this.converter = new FITSMetaDataConverter();
    }
    
    @Test
    public void shouldExtractData() throws Exception {
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File("src/test/resources/fits.xml"));
            this.converter.extractValues(document);
            
            //TODO assertions
            
        } catch (DocumentException e) {
            Assert.fail(e.getMessage());
        }
    }
}
