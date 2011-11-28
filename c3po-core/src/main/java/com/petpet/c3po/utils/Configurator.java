package com.petpet.c3po.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.db.DBManager;

public final class Configurator {

    private static final Logger LOG = LoggerFactory.getLogger(Configurator.class);

    private static Configurator uniqueInstance;

    public static synchronized Configurator getInstance() {
        if (Configurator.uniqueInstance == null) {
            Configurator.uniqueInstance = new Configurator();
        }

        return Configurator.uniqueInstance;
    }

    public void configure() {
        this.connectToDatabase();
        this.loadKnownProperties();
        // eventually load mapping of properties, e.g. lastModified maps to
        // lastChanged
        // TODO load properties files and setup preferences
    }

    private void connectToDatabase() {
    	LOG.debug("connection to database");
        DBManager.getInstance();
    }

    private void loadKnownProperties() {
    	LOG.debug("loading known properties");
            List<Property> props = DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.ALL_PROPERTIES_QUERY, Property.class).getResultList();
            
            for (Property p : props) {
                Helper.KNOWN_PROPERTIES.put(p.getName(), p);
            }
    }

    private Configurator() {
    }
}
