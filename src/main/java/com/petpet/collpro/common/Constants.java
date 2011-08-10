package com.petpet.collpro.common;

import java.util.Map;

import com.petpet.collpro.datamodel.Property;

public final class Constants {

    public static final String XML_SCHEMA_PROPERTY = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    public static final String XML_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";
    
    public static final String ALL_PROPERTIES_QUERY = "getAllProperties";
    
    public static final String VALUES_BY_NAME_QUERY = "getValueByName";
    
    public static final String ELEMENTS_COUNT_QUERY = "getElementsCount";
    
    public static final String ELEMENTS_WITH_PROPERTY_COUNT_QUERY = "getElementsWithPropertyCount";
    
    public static final String ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY = "getElementsWithPropertyAndValueCount";
    
    public static final String ELEMENTS_WITH_DISTINCT_PROPERTY_AND_VALUE_COUNT_QUERY = "getElementsWithDistinctPropertyValueCount";
    
    public static final String DISTINCT_PROPERTY_VALUES_SET_QUERY = "getDistinctPropertyValuesSet";
    
    public static Map<String, Property> KNOWN_PROPERTIES;
    
    private Constants() {
        
    }
}
