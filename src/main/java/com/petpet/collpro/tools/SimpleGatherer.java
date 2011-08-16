package com.petpet.collpro.tools;

import java.io.File;
import java.io.FileFilter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.db.DBManager;
import com.petpet.collpro.metadata.converter.IMetaDataConverter;

public class SimpleGatherer {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleGatherer.class);
    
    private IMetaDataConverter converter;
    
    public SimpleGatherer(IMetaDataConverter converter) {
        this.converter = converter;
    }
    
    public void gather(File dir) {
        if (dir == null || !dir.isDirectory()) {
            LOG.warn("Provided folder is null or not a folder");
        }
        
        File[] files = dir.listFiles(new XMLFileFilter());
        for (File f : files) {
            this.extractMetaData(f);
        }
    }
    
    private void extractMetaData(File f) {
        if (f.isFile()) {
            try {
                SAXReader reader = new SAXReader();
                Document document = reader.read(f);
                Element element = this.converter.extractValues(document);
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
