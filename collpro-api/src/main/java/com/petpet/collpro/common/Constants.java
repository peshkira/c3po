package com.petpet.collpro.common;


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

    /*
     * Defined in Property.java
     */
    /**
     * The name of the named query that retrieves all properties known to the
     * system.
     */
    public static final String ALL_PROPERTIES_QUERY = "getAllProperties";

    /*
     * Defined in Value.java
     */
    /**
     * The name of the named query that retrieves all properties in a
     * collection.
     */
    public static final String ALL_COLLECTION_PROPERTIES_QUERY = "getAllPropertiesInCollection";

    /*
     * Defined in Value.java
     */
    /**
     * The name of the named query that retrieves all values of a property with
     * a certain name. Requires two parameters :pname and :coll for the property
     * name and the collection respectively.
     */
    public static final String COLLECTION_VALUES_BY_PROPERTY_NAME_QUERY = "getValueByPropertyNameAndCollection";

    public static final String VALUES_BY_NAME_AND_VALUE = "getValueByPropertyAndValue";

    public static final String COLLECTION_VALUES_BY_NAME_AND_VALUE = "getValueByPropertyAndValueAndCollection";

    /*
     * Defined in Element.java
     */
    /**
     * The name of the named query that counts all elements in a collection.
     * Requires one parameter :coll.
     */
    public static final String COLLECTION_ELEMENTS_COUNT_QUERY = "getElementsInCollectionCount";

    /*
     * Defined in Value.java
     */
    /**
     * The name of the named query that counts all elements with a specific
     * property. Requires two parameters :pname for the property name and :coll
     * for the collection.
     */
    public static final String COLLECTION_ELEMENTS_WITH_PROPERTY_COUNT_QUERY = "getElementsWithPropertyInCollectionCount";

    /*
     * Defined in Value.java
     */
    /**
     * The name of the named query that counts all elements with a specific
     * property and value for this property. Requires three parameters :pname,
     * :value and :coll for the property name, value and the collection
     * respectively.
     */
    public static final String COLLECTION_ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY = "getElementsWithPropertyAndValueInCollectionCount";
    
    public static final String COLLECTION_ELEMENTS_WITH_PROPERTY_AND_VALUE_SET_QUERY = "getElementsWithPropertyAndValueInCollectionSet";

    /*
     * Defined in Value.java
     */
    /**
     * The name of the named query that counts how many distinct values for a
     * specific property exist within a collection. E.g. how many different
     * mime-types exist. Requires two parameters :name for the property name and
     * :coll for the collection.
     */
    public static final String COLLECTION_DISTINCT_PROPERTY_VALUE_COUNT_QUERY = "getDistinctPropertyValueInCollectionCount";

    /*
     * Defined in Value.java
     */
    /**
     * The name of the named query that retrieves a set of the different values
     * for a specific property. E.g. what are the different mime-types. Requires
     * two parameters :pname for the property name and :coll for the collection.
     */
    public static final String COLLECTION_DISTINCT_PROPERTY_VALUES_SET_QUERY = "getDistinctPropertyValuesInCollectionSet";
    
    public static final String COLLECTION_DISTINCT_VALUES_IN_FILTERED = "getDistinctValuesWithinPropertyFilteredCollection";

    /*
     * Defined in Value.java
     */
    /**
     * The name of the named query that counts the values for a specific
     * element. Requires one parameter ':element' for the element object.
     */
    public static final String VALUES_FOR_ELEMENT_COUNT = "getAllValuesForElementCount";

    /*
     * Defined in Value.java
     */
    /**
     * The name of the named query that retrieves a set of values for a specific
     * element. Requires one parameter ':element' for the element object.
     */
    public static final String VALUES_FOR_ELEMENT = "getAllValuesForElement";

    /*
     * Defined in Value.java
     */
    /**
     * The name of the named query that retrieves the property ids and property
     * names and their occurrences throughout the collection in a descending
     * order. Requires one parameter :coll for the collection.
     */
    public static final String COLLECTION_MOST_OCCURRING_PROPERTIES = "getMostOccurringPropertiesInCollection";

    /*
     * Defined in NumericValue.java
     */
    /**
     * The name of the named query that sums the values of the numeric
     * properties with the given name. Requires two parameters <i>:pname</i> and
     * <i>:coll</i> for the property name and the collection respectively.
     */
    public static final String COLLECTION_SUM_VALUES_FOR_PROPERTY = "getSumOfValuesForPropertyInCollection";

    /*
     * Defined in NumericValue.java
     */
    /**
     * The name of the named query that calculates the average of the values of
     * the numeric properties with the given name. Requires two parameters
     * <i>:pname</i> and <i>:coll</i> for the property name and the collection
     * respectively.
     */
    public static final String COLLECTION_AVG_VALUES_FOR_PROPERTY = "getAvgOfValuesForPropertyInCollection";

    /*
     * Defined in Value.java
     */
    /**
     * The name of the named query that retrieves the distribution of all values
     * within the collection. The query returns a list of Object[] arrays that
     * have length of three. The first Object is the property name, the second -
     * the value and the third the occurrences of this specific value. Requires
     * one parameter <i>:coll</i> for the collection.
     */
    public static final String COLLECTION_VALUES_DISTRIBUTION = "getAllValuesInCollectionDistribution";

    /*
     * Defined in Value.java
     */
    /**
     * The name of the named query that retrieves the values distribution of a
     * specific property within the collection. The query returns a list of
     * Object[] arrays that have length of three. The first Object is the
     * property name, the second - the value and the third the occurrences of
     * this specific value. Requires two parameters <i>:pname</i> and
     * <i>:coll</i> for the property name and the collection.
     */
    public static final String COLLECTION_SPECIFIC_VALUE_DISTRIBUTION = "getSpecificValueInCollectionDistribution";

    private Constants() {

    }
}
