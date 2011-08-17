package com.petpet.collpro.tools;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.db.DBManager;

import junit.framework.Assert;

public class FITSMetaDataConverterTest {
    
    private FITSMetaDataConverter converter;
    
    @Before
    public void before() {
        this.converter = new FITSMetaDataConverter();
    }
    
    @After
    public void after() {
        DBManager.getInstance().close();
        DBManager.getInstance().createEntityManagerFactory(); // reset db
    }
    
    @Test
    public void shouldExtractData() throws Exception {
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File("src/test/resources/fits.xml"));
            Element e = this.converter.extractValues(document);
            Assert.assertEquals(23, e.getValues().size());
            
        } catch (DocumentException e) {
            Assert.fail(e.getMessage());
        }
    }
}
