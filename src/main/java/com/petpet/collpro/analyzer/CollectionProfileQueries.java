package com.petpet.collpro.analyzer;

import java.util.List;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.datamodel.Value;
import com.petpet.collpro.datamodel.StringValue;
import com.petpet.collpro.db.DBManager;

public class CollectionProfileQueries {
    
    public List<Property> getAllProperties() {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.ALL_PROPERTIES_QUERY,
            Property.class).getResultList();
    }
    
    public List<Property> getAllPropertiesInCollection(DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.ALL_COLLECTION_PROPERTIES_QUERY,
            Property.class).setParameter("coll", collection).getResultList();
    }
    
    public List<Value> getValuesByPropertyName(String pname) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.VALUES_BY_PROPERTY_NAME_QUERY,
            Value.class).setParameter("pname", pname).getResultList();
    }
    
    public List<Value> getValuesByPropertyName(String pname, DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.COLLECTION_VALUES_BY_PROPERTY_NAME_QUERY, Value.class).setParameter("pname", pname).setParameter(
            "coll", collection).getResultList();
    }
    
    public List<Value> getValueByPropertyNameAndValue(String pname, String value) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.VALUES_BY_NAME_AND_VALUE,
            Value.class).setParameter("pname", pname).setParameter("value", value).getResultList();
    }
    
    public List<Value> getValueByPropertyNameAndValue(String pname, String value, DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.COLLECTION_VALUES_BY_NAME_AND_VALUE, Value.class).setParameter("pname", pname).setParameter(
            "value", value).setParameter("coll", collection).getResultList();
    }
    
    public long getElementsCount() {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(Constants.ELEMENTS_COUNT_QUERY)
            .getSingleResult();
    }
    
    public long getElementsCount(DigitalCollection collection) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.COLLECTION_ELEMENTS_COUNT_QUERY).setParameter("coll", collection).getSingleResult();
    }
    
    public long getElementsWithPropertyCount(String pname) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.ELEMENTS_WITH_PROPERTY_COUNT_QUERY).setParameter("pname", pname).getSingleResult();
    }
    
    public long getElementsWithPropertyCount(String pname, DigitalCollection collection) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.COLLECTION_ELEMENTS_WITH_PROPERTY_COUNT_QUERY).setParameter("pname", pname).setParameter("coll",
            collection).getSingleResult();
    }
    
    public long getElementsWithPropertyAndValueCount(String pname, String value) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY).setParameter("pname", pname).setParameter("value",
            value).getSingleResult();
    }
    
    public long getElementsWithPropertyAndValueCount(String pname, String value, DigitalCollection coll) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.COLLECTION_ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY).setParameter("pname", pname)
            .setParameter("value", value).setParameter("coll", coll).getSingleResult();
    }
    
    public long getDistinctPropertyValueCount(String pname) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.DISTINCT_PROPERTY_VALUE_COUNT_QUERY).setParameter("pname", pname).getSingleResult();
    }
    
    public long getDistinctPropertyValueCount(String pname, DigitalCollection collection) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.COLLECTION_DISTINCT_PROPERTY_VALUE_COUNT_QUERY).setParameter("pname", pname).setParameter("coll",
            collection).getSingleResult();
    }
    
    public List<String> getDistinctPropertyValueSet(String pname) {
        return DBManager.getInstance().getEntityManager()
            .createNamedQuery(Constants.DISTINCT_PROPERTY_VALUES_SET_QUERY).setParameter("pname", pname)
            .getResultList();
    }
    
    public List<String> getDistinctPropertyValueSet(String pname, DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.COLLECTION_DISTINCT_PROPERTY_VALUES_SET_QUERY).setParameter("pname", pname).setParameter("coll",
            collection).getResultList();
    }
    
    public long getValuesForElementCount(Element e) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(Constants.VALUES_FOR_ELEMENT_COUNT)
            .setParameter("element", e).getSingleResult();
    }
    
    public List<Value> getValuesForElement(Element e) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.VALUES_FOR_ELEMENT, Value.class)
            .setParameter("element", e).getResultList();
    }
    
    public List<Object[]> getMostOccurringProperties(int limit) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.MOST_OCCURRING_PROPERTIES)
            .setMaxResults(limit).getResultList();
    }
    
    public List<Object[]> getMostOccurringProperties(int limit, DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.COLLECTION_MOST_OCCURRING_PROPERTIES).setParameter("coll", collection).setMaxResults(limit)
            .getResultList();
    }
    
    public long getSumOfNumericProperty(String pname) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(Constants.SUM_VALUES_FOR_PROPERTY)
            .setParameter("pname", pname).getSingleResult();
    }
    
    public long getSumOfNumericProperty(String pname, DigitalCollection collection) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.COLLECTION_SUM_VALUES_FOR_PROPERTY).setParameter("pname", pname).setParameter("coll", collection)
            .getSingleResult();
    }
    
    public double getAverageOfNumericProperty(String pname) {
        return (Double) DBManager.getInstance().getEntityManager().createNamedQuery(Constants.AVG_VALUES_FOR_PROPERTY)
            .setParameter("pname", pname).getSingleResult();
    }
    
    public double getAverageOfNumericProperty(String pname, DigitalCollection collection) {
        return (Double) DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.COLLECTION_AVG_VALUES_FOR_PROPERTY).setParameter("pname", pname).setParameter("coll", collection)
            .getSingleResult();
    }
    
    public List<Object[]> getValuesDistribution() {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.VALUES_DISTRIBUTION)
            .getResultList();
    }
    
    public List<Object[]> getValuesDistribution(DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.COLLECTION_VALUES_DISTRIBUTION)
            .setParameter("coll", collection).getResultList();
    }
    
    public List<Object[]> getSpecificPropertyValuesDistribution(String pname) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.SPECIFIC_VALUE_DISTRIBUTION)
            .setParameter("pname", pname).getResultList();
    }
    
    public List<Object[]> getSpecificPropertyValuesDistribution(String pname, DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.COLLECTION_SPECIFIC_VALUE_DISTRIBUTION).setParameter("pname", pname).setParameter("coll",
            collection).getResultList();
    }
    
    public void getDoubleFilteredSortedCollection(String pname1, String pname2) {
        
        List<String> filter1 = this.getDistinctPropertyValueSet(pname1);
        
        for (String f1 : filter1) {
            System.out.println(pname1 + ": " + f1);
            List<Element> elements = DBManager.getInstance().getEntityManager().createNamedQuery("getElementsWithPropertyAndValueSet",
                Element.class).setParameter("pname", pname1).setParameter("value", f1).getResultList();
            for (Element e : elements) {
                System.out.println("\t" + e.getName());
            }
        }
        
//        List<Element> list = DBManager
//            .getInstance()
//            .getEntityManager()
//            .createQuery(
//                "SELECT s.element FROM Value s WHERE s.property.name = :pname2 AND s.element IN (SELECT v.element FROM Value v WHERE v.property.name = :pname1)")
//            .setParameter("pname2", pname2).setParameter("pname1", pname1).getResultList();
//        
//        for (Element o : list) {
//            System.out.println(o.getName());
//        }
    }
}
