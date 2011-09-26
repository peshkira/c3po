package com.petpet.collpro.tools;

import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.db.DBManager;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FITSMetaDataConverterTest implements ChangeListener {

    private FITSMetaDataConverter converter;
    private DigitalCollection collection;

    @Before
    public void before() {
        this.converter = new FITSMetaDataConverter();
        this.collection = new DigitalCollection("Test");
        DBManager.getInstance().persist(this.collection);
        
    }

    @After
    public void after() {
        DBManager.getInstance().close();
        DBManager.getInstance().createEntityManagerFactory(); // reset db
    }

    @Test
    public void shouldExtractData() throws Exception {
        File[] files = { new File("src/test/resources/fits.xml") };
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("config.date", new Date());
        config.put("config.collection", this.collection);
        config.put("config.fits_files", files);
        
        this.converter.addObserver(this);
        this.converter.configure(config);
        this.converter.execute();

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == null) {
            Assert.fail("No element returned");
        } else {
            Assert.assertEquals(((Element) e.getSource()).getName(), "About Stacks.pdf");
        }
        
    }
}
