package com.petpet.collpro.analyzer;

import java.util.List;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.datamodel.Value;
import com.petpet.collpro.db.DBManager;

public class CollectionProfileAnalyzer {
    
    public List<Property> getAllProperties() {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.ALL_PROPERTIES_QUERY,
            Property.class).getResultList();
    }
    
    public List<Value> getValuesByPropertyName(String pname) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.VALUES_BY_PROPERTY_NAME_QUERY,
            Value.class).setParameter("pname", pname).getResultList();
    }
    
    public List<Value> getValueByPropertyNameAndValue(String pname, String value) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.VALUES_BY_NAME_AND_VALUE,
            Value.class).setParameter("pname", pname).setParameter("value", value).getResultList();
    }
    
    public long getElementsCount() {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(Constants.ELEMENTS_COUNT_QUERY)
            .getSingleResult();
    }
    
    public long getElementsWithPropertyCount(String pname) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.ELEMENTS_WITH_PROPERTY_COUNT_QUERY).setParameter("pname", pname).getSingleResult();
    }
    
    public long getElementsWithPropertyAndValueCount(String pname, String value) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY).setParameter("pname", pname).setParameter("value",
            value).getSingleResult();
    }
    
    public long getDistinctPropertyValueCount(String pname) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.DISTINCT_PROPERTY_VALUE_COUNT_QUERY).setParameter("pname", pname).getSingleResult();
    }
    
    public List<String> getDistinctPropertyValueSet(String pname) {
        return DBManager.getInstance().getEntityManager()
            .createNamedQuery(Constants.DISTINCT_PROPERTY_VALUES_SET_QUERY).setParameter("pname", pname)
            .getResultList();
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
    
    public long getSumOfNumericProperty(String pname) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(Constants.SUM_VALUES_FOR_PROPERTY)
            .setParameter("pname", pname).getSingleResult();
    }
    
    public double getAverageOfNumericProperty(String pname) {
        return (Double) DBManager.getInstance().getEntityManager().createNamedQuery(Constants.AVG_VALUES_FOR_PROPERTY)
            .setParameter("pname", pname).getSingleResult();
    }
    
    public List<Object[]> getValuesDistribution() {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.VALUES_DISTRIBUTION)
            .getResultList();
    }
    
    public List<Object[]> getSpecificPropertyValuesDistribution(String pname) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.SPECIFIC_VALUE_DISTRIBUTION)
            .setParameter("pname", pname).getResultList();
    }
}
