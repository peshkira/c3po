package com.petpet.collpro.tools;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.db.DBManager;
import com.petpet.collpro.metadata.converter.IMetaDataConverter;

public class SimpleGatherer {
    
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
        
        File[] files = dir.listFiles(new XMLFileFilter());
        for (File f : files) {
            this.extractMetaData(f);
        }
        
        DBManager.getInstance().persist(this.collection);
    }
    
    private void extractMetaData(File f) {
        if (f.isFile()) {
            try {
                SAXReader reader = new SAXReader();
                Document document = reader.read(f);
                Element element = this.converter.extractValues(document);
                element.setCollection(this.collection);
                DBManager.getInstance().persist(element);
                
            } catch (DocumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    
    private class XMLFileFilter implements FileFilter {
        
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".xml");
        }
        
    }
    
}
