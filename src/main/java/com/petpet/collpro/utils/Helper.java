package com.petpet.collpro.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.petpet.collpro.datamodel.BooleanValue;
import com.petpet.collpro.datamodel.NumericValue;
import com.petpet.collpro.datamodel.PropertyType;
import com.petpet.collpro.datamodel.StringValue;
import com.petpet.collpro.datamodel.Value;

public final class Helper {
    
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
}
