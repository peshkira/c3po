package com.petpet.collpro.tools;

import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.db.DBManager;
import com.petpet.collpro.metadata.converter.IMetaDataConverter;
import com.petpet.collpro.utils.ConfigurationException;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleGatherer implements ChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleGatherer.class);

    private IMetaDataConverter converter;

    private DigitalCollection collection;

    @Deprecated
    public SimpleGatherer(IMetaDataConverter converter) {
        this.converter = converter;
    }

    public SimpleGatherer(IMetaDataConverter converter, DigitalCollection collection) {
        this(converter);

        if (collection == null) {
            this.collection = new DigitalCollection("collection-" + new Date().toString());
        } else {
            this.collection = collection;
        }

        DBManager.getInstance().persist(this.collection);
    }

    public void gather(File dir) {
        if (dir == null || !dir.isDirectory()) {
            LOG.warn("Provided folder is null or not a folder");
        }

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("config.date", new Date());
        config.put("config.collection", this.collection);
        config.put("cofig.fits_files", dir.listFiles(new XMLFileFilter()));

        try {
            this.converter.addObserver(this);
            this.converter.configure(config);
            this.converter.convert();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

    }

    private class XMLFileFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".xml");
        }

    }

    @Override
    public void stateChanged(ChangeEvent evt) {
        Object o = evt.getSource();
        if (o != null) {
            DBManager.getInstance().persist(o);
        }

    }

}
