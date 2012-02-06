package com.petpet.c3po.gatherer;


import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.Call;
import com.petpet.c3po.api.ITool;
import com.petpet.c3po.api.Message;
import com.petpet.c3po.api.MetaDataGatherer;
import com.petpet.c3po.common.Config;
import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.db.DBManager;

public class FileSystemGatherer implements MetaDataGatherer, Call {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemGatherer.class);

    private ITool converter;

    private DigitalCollection collection;

    public FileSystemGatherer(ITool converter, DigitalCollection collection) {
        this.converter = converter;

        if (collection == null) {
            this.collection = new DigitalCollection("collection-" + new Date().toString());
        } else {
            this.collection = collection;
        }

        DBManager.getInstance().persist(this.collection);
    }

    public void gather(File dir) {
        if (dir == null || !dir.isDirectory()) {
            LOG.warn("Provided folder is null or not a folder, skipping");
            
        } else {
            this.converter.addParameter(Config.DATE_CONF, new Date())
                    .addParameter(Config.COLLECTION_CONF, this.collection)
                    .addParameter(Config.FITS_FILES_CONF, dir.listFiles(new XMLFileFilter()));
            this.converter.addObserver(this);
            this.converter.execute();
        }

    }

    private class XMLFileFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".xml");
        }

    }

    @Override
    public void back(Message<? extends Object> n) {
        Object o = n.getData();
        if (o != null && o.getClass().equals(Element.class)) {
            DBManager.getInstance().persist(o);
        }

    }

}