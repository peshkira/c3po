package com.petpet.collpro.datamodel;

public enum PropertyType {
    DEFAULT, BOOL, NUMERIC, STRING, ARRAY;

    public static PropertyType getTypeFromString(String t) {
        if (t == null) {
            return DEFAULT;
        }
        
        if (t.equals(BOOL.name())) {
            return BOOL;
        }
        
        if (t.equals(NUMERIC.name())) {
            return NUMERIC;
        }
        
        if (t.equals(STRING.name())) {
            return STRING;
        }
        
        if (t.equals(ARRAY.name())) {
            return ARRAY;
        }
        
        return DEFAULT;
    }
}
