package com.petpet.collpro.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.datamodel.BooleanValue;
import com.petpet.collpro.datamodel.NumericValue;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.datamodel.PropertyType;
import com.petpet.collpro.datamodel.StringValue;
import com.petpet.collpro.datamodel.Value;
import com.petpet.collpro.db.DBManager;

public final class Helper {
    
    private static final Logger LOG = LoggerFactory.getLogger(Helper.class);
    
    private static Properties TYPES;
    
    static {
        TYPES = new Properties();
        InputStream in;
        try {
            in = new FileInputStream("src/main/resources/property_types.properties");
            TYPES.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Helper() {
        
    }
    
    public static Value getTypedValue(PropertyType type, String value) {
        
        switch (type) {
            case ARRAY:
                return null;
            case BOOL:
                return new BooleanValue(value);
            case NUMERIC:
                return new NumericValue(value);
            case STRING: // empty on purpose
            case DEFAULT: // empty on purpose
            default:
                return new StringValue(value);
        }
    }
    
    public static PropertyType getType(String name) {
        String t = (String) TYPES.get(name);
        return PropertyType.getTypeFromString(t);
    }
    
    public static Property getPropertyByName(String name) {
        Property p = Constants.KNOWN_PROPERTIES.get(name);
        
        if (p == null) {
            p = new Property();
            p.setName(name);
            p.setType(Helper.getType(p.getName()));
            Constants.KNOWN_PROPERTIES.put(p.getName(), p);
        }
        
        return p;
    }
    
    public static boolean isElementAlreadyProcessed(String md5) {
        if (md5 == null || md5.equals("")) {
            LOG.warn("No checksum provided, assuming element is not processed.");
            return false;
        }
        
        boolean isDone = false;
        LOG.debug("MD5: {}", md5);
        
        try {
            DBManager.getInstance().getEntityManager().createNamedQuery(Constants.VALUES_BY_NAME_AND_VALUE).setParameter("pname", "md5checksum")
                .setParameter("value", md5).getSingleResult();
            isDone = true;
        } catch (NoResultException nre) {
            LOG.debug("No element with this checksum ingested, continue processing.");
            isDone = false;
            
        } catch (NonUniqueResultException nue) {
            LOG.warn("More than one elements with this checksum are already processed. Please inspect");
            isDone = true;
        }
        
        return isDone;
    }
}
