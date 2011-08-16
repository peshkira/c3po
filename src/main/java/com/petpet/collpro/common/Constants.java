package com.petpet.collpro.common;

import java.util.HashMap;
import java.util.Map;

import com.petpet.collpro.datamodel.Property;

public final class Constants {
    
    /**
     * The url for the xml schema property used by the sax parser while
     * validating xml files against their schemata.
     */
    public static final String XML_SCHEMA_PROPERTY = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    
    /**
     * The url for the xml schema language used by the sax parser while
     * validating xml files against their schemata.
     */
    public static final String XML_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";
    
    /**
     * The name of the named query that retrieves all properties known to the
     * system.
     */
    public static final String ALL_PROPERTIES_QUERY = "getAllProperties";
    
    /**
     * The name of the named query that retrieves all values of a property with
     * a certain name. Requires one parameter :name.
     */
    public static final String VALUES_BY_NAME_QUERY = "getValueByName";
    
    /**
     * The name of the named query that counts all elements.
     */
    public static final String ELEMENTS_COUNT_QUERY = "getElementsCount";
    
    /**
     * The name of the named query that counts all elements with a specific
     * property. Requires one parameter :pname for the property name.
     */
    public static final String ELEMENTS_WITH_PROPERTY_COUNT_QUERY = "getElementsWithPropertyCount";
    
    /**
     * The name of the named query that counts all elements with a specific
     * property and value for this property. Requires two parameters :pname and
     * :value.
     */
    public static final String ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY = "getElementsWithPropertyAndValueCount";
    
    /**
     * The name of the named query that counts how many distinct values for a
     * specific property exist. E.g. how many different mime-types exist.
     * Requires one parameter :name for the property name.
     */
    public static final String DISTINCT_PROPERTY_VALUE_COUNT_QUERY = "getDistinctPropertyValueCount";
    
    /**
     * The name of the named query that retrieves a set of the different values
     * for a specific property. E.g. what are the different mime-types. Requires
     * one parameter :name for the property name.
     */
    public static final String DISTINCT_PROPERTY_VALUES_SET_QUERY = "getDistinctPropertyValuesSet";
    
    /**
     * The name of the named query that counts the values for a specific
     * element. Requires one parameter ':element' for the element object.
     */
    public static final String VALUES_FOR_ELEMENT_COUNT = "getAllValuesForElementCount";
    
    /**
     * The name of the named query that retrieves a set of values for a specific
     * element. Requires one parameter ':element' for the element object.
     */
    public static final String VALUES_FOR_ELEMENT = "getAllValuesForElement";
    
    /**
     * A map with the known properties. It is populated by the configurator
     * usually at startup.
     */
    public static Map<String, Property> KNOWN_PROPERTIES = new HashMap<String, Property>();
    
    private Constants() {
        
    }
}
