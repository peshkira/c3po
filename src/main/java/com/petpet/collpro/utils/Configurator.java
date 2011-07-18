package com.petpet.collpro.utils;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.db.DBManager;

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
        DBManager.getInstance();
    }

    private void loadKnownProperties() {
//        Constants.KNOWN_PROPERTIES; 
            
            List<Property> props = DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.ALL_PROPERTIES_QUERY, Property.class).getResultList();
            
            Constants.KNOWN_PROPERTIES = new HashMap<String, Property>();
            for (Property p : props) {
                Constants.KNOWN_PROPERTIES.put(p.getName(), p);
            }
    }

    private Configurator() {
    }
}
