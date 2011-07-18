package com.petpet.collpro.common;

import java.util.List;

import com.petpet.collpro.datamodel.Property;

public final class Constants {

    public static final String XML_SCHEMA_PROPERTY = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    public static final String XML_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";
    
    public static final String ALL_PROPERTIES_QUERY = "ALL_PROPERTIES";
    
    public static List<Property> KNOWN_PROPERTIES;
    
    private Constants() {
        
    }
}
