package com.petpet.collpro.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.datamodel.BooleanValue;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.FloatValue;
import com.petpet.collpro.datamodel.IntegerValue;
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
    
    /**
     * A map with the known properties. It is populated by the configurator
     * usually at startup.
     */
    public static Map<String, Property> KNOWN_PROPERTIES = new HashMap<String, Property>();
    
    private Helper() {
        
    }
    
    public static Value getTypedValue(PropertyType type, String value) {
        
        switch (type) {
            case ARRAY:
                return null;
            case BOOL:
                return new BooleanValue(value);
            case NUMERIC:
                return new IntegerValue(value);
            case FLOAT:
              return new FloatValue(value);
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
        Property p = Helper.KNOWN_PROPERTIES.get(name);
        
        if (p == null) {
            p = new Property();
            p.setName(name);
            p.setType(Helper.getType(p.getName()));
            Helper.KNOWN_PROPERTIES.put(p.getName(), p);
        }
        
        return p;
    }
    
    public static List<Property> getPropertiesByNames(String... names) {
      List<Property> result = new ArrayList<Property>();
      for (String n : names) {
        Property p = Helper.KNOWN_PROPERTIES.get(n);
        if (p != null) {
          result.add(p);
        }
      }
      
      return result;
    }
    
    public static boolean isElementAlreadyProcessed(DigitalCollection coll, String md5) {
        if (md5 == null || md5.equals("")) {
            LOG.warn("No checksum provided, assuming element is not processed.");
            return false;
        }
        
        boolean isDone = false;
        LOG.debug("MD5: {}", md5);
        
        try {
            DBManager.getInstance().getEntityManager().createNamedQuery(Constants.COLLECTION_VALUES_BY_NAME_AND_VALUE).setParameter("pname", "md5checksum")
                .setParameter("value", md5).setParameter("coll", coll).getSingleResult();
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
