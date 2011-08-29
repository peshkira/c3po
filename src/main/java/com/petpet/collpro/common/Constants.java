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
    /*
     * Defined in Property.java
     */
    public static final String ALL_PROPERTIES_QUERY = "getAllProperties";
    
    /**
     * The name of the named query that retrieves all properties in a
     * collection.
     */
    /*
     * Defined in Value.java
     */
    public static final String ALL_COLLECTION_PROPERTIES_QUERY = "getAllPropertiesInCollection";
    
    /**
     * The name of the named query that retrieves all values of a property with
     * a certain name. Requires one parameter :pname.
     */
    /*
     * Defined in Value.java
     */
    public static final String VALUES_BY_PROPERTY_NAME_QUERY = "getValueByPropertyName";
    
    public static final String COLLECTION_VALUES_BY_PROPERTY_NAME_QUERY = "getValueByPropertyNameAndCollection";
    
    public static final String VALUES_BY_NAME_AND_VALUE = "getValueByPropertyAndValue";
    
    public static final String COLLECTION_VALUES_BY_NAME_AND_VALUE = "getValueByPropertyAndValueAndCollection";
    
    /**
     * The name of the named query that counts all elements.
     */
    /*
     * Defined in Element.java
     */
    public static final String ELEMENTS_COUNT_QUERY = "getElementsCount";
    
    public static final String COLLECTION_ELEMENTS_COUNT_QUERY = "getElementsInCollectionCount";
    
    /**
     * The name of the named query that counts all elements with a specific
     * property. Requires one parameter :pname for the property name.
     */
    /*
     * Defined in Value.java
     */
    public static final String ELEMENTS_WITH_PROPERTY_COUNT_QUERY = "getElementsWithPropertyCount";
    
    public static final String COLLECTION_ELEMENTS_WITH_PROPERTY_COUNT_QUERY = "getElementsWithPropertyInCollectionCount";
    
    /**
     * The name of the named query that counts all elements with a specific
     * property and value for this property. Requires two parameters :pname and
     * :value.
     */
    /*
     * Defined in Value.java
     */
    public static final String ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY = "getElementsWithPropertyAndValueCount";
    
    public static final String COLLECTION_ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY = "getElementsWithPropertyAndValueInCollectionCount";
    
    /**
     * The name of the named query that counts how many distinct values for a
     * specific property exist. E.g. how many different mime-types exist.
     * Requires one parameter :name for the property name.
     */
    /*
     * Defined in Value.java
     */
    public static final String DISTINCT_PROPERTY_VALUE_COUNT_QUERY = "getDistinctPropertyValueCount";
    
    public static final String COLLECTION_DISTINCT_PROPERTY_VALUE_COUNT_QUERY = "getDistinctPropertyValueInCollectionCount";
    
    /**
     * The name of the named query that retrieves a set of the different values
     * for a specific property. E.g. what are the different mime-types. Requires
     * one parameter :name for the property name.
     */
    /*
     * Defined in Value.java
     */
    public static final String DISTINCT_PROPERTY_VALUES_SET_QUERY = "getDistinctPropertyValuesSet";
    
    public static final String COLLECTION_DISTINCT_PROPERTY_VALUES_SET_QUERY = "getDistinctPropertyValuesInCollectionSet";
    
    /**
     * The name of the named query that counts the values for a specific
     * element. Requires one parameter ':element' for the element object.
     */
    /*
     * Defined in Value.java
     */
    public static final String VALUES_FOR_ELEMENT_COUNT = "getAllValuesForElementCount";
    
    /**
     * The name of the named query that retrieves a set of values for a specific
     * element. Requires one parameter ':element' for the element object.
     */
    /*
     * Defined in Value.java
     */
    public static final String VALUES_FOR_ELEMENT = "getAllValuesForElement";
    
    /**
     * The name of the named query that retrieves the property ids and property
     * names and their occurrences throughout the collection in a descending
     * order.
     */
    /*
     * Defined in Value.java
     */
    public static final String MOST_OCCURRING_PROPERTIES = "getMostOccurringProperties";
    
    public static final String COLLECTION_MOST_OCCURRING_PROPERTIES = "getMostOccurringPropertiesInCollection";
    
    /**
     * The name of the named query that sums the values of the numeric
     * properties with the given name. Requires one parameter <i>:pname</i>.
     */
    public static final String SUM_VALUES_FOR_PROPERTY = "getSumOfValuesForProperty";
    
    public static final String COLLECTION_SUM_VALUES_FOR_PROPERTY = "getSumOfValuesForPropertyInCollection";
    
    /**
     * The name of the named query that calculates the average of the values of
     * the numeric properties with the given name. Requires one parameter
     * <i>:pname</i>.
     */
    /*
     * Defined in NumericValue.java
     */
    public static final String AVG_VALUES_FOR_PROPERTY = "getAvgOfValuesForProperty";
    
    public static final String COLLECTION_AVG_VALUES_FOR_PROPERTY = "getAvgOfValuesForPropertyInCollection";
    
    public static final String VALUES_DISTRIBUTION = "getAllValuesDistribution";
    
    public static final String COLLECTION_VALUES_DISTRIBUTION = "getAllValuesInCollectionDistribution";
    
    public static final String SPECIFIC_VALUE_DISTRIBUTION = "getSpecificValueDistribution";
    
    public static final String COLLECTION_SPECIFIC_VALUE_DISTRIBUTION = "getSpecificValueInCollectionDistribution";
    
    /**
     * A map with the known properties. It is populated by the configurator
     * usually at startup.
     */
    /*
     * Defined in NumericValue.java
     */
    public static Map<String, Property> KNOWN_PROPERTIES = new HashMap<String, Property>();
    
    private Constants() {
        
    }
}
