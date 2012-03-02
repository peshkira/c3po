package com.petpet.c3po.db;

import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.Value;

public class PreparedQueries {

  private static final Logger LOGGER = LoggerFactory.getLogger(PreparedQueries.class);

  private EntityManager em;

  public PreparedQueries() {

  }

  public PreparedQueries(EntityManager em) {
    this();
    this.em = em;

    if (this.em == null) {
      LOGGER.warn("The entity manager is null, please set one");
    }
  }

  public List<Property> getAllProperties() {
    return this.getEntityManager().createNamedQuery(Constants.ALL_PROPERTIES_QUERY, Property.class).getResultList();
  }

  public List<String> getAllPropertyNames() {
    return this.getEntityManager().createNamedQuery(Constants.ALL_PROPERTY_NAMES_QUERY, String.class).getResultList();
  }

  public List<Property> getAllPropertiesInCollection(DigitalCollection collection) {
    return this.getEntityManager().createNamedQuery(Constants.ALL_COLLECTION_PROPERTIES_QUERY, Property.class)
        .setParameter("coll", collection).getResultList();
  }

  @SuppressWarnings("rawtypes")
  public List<Value> getValuesByPropertyName(String pname, DigitalCollection collection) {
    return this.getEntityManager().createNamedQuery(Constants.COLLECTION_VALUES_BY_PROPERTY_NAME_QUERY, Value.class)
        .setParameter("pname", pname).setParameter("coll", collection).getResultList();
  }

  @SuppressWarnings("rawtypes")
  public List<Value> getValueByPropertyNameAndValue(String pname, String value, DigitalCollection collection) {
    return this.getEntityManager().createNamedQuery(Constants.COLLECTION_VALUES_BY_NAME_AND_VALUE, Value.class)
        .setParameter("pname", pname).setParameter("value", value).setParameter("coll", collection).getResultList();
  }

  public long getElementsCount(DigitalCollection collection) {
    return (Long) this.getEntityManager().createNamedQuery(Constants.COLLECTION_ELEMENTS_COUNT_QUERY)
        .setParameter("coll", collection).getSingleResult();
  }

  public long getElementsWithPropertyCount(String pname, DigitalCollection collection) {
    return (Long) this.getEntityManager().createNamedQuery(Constants.COLLECTION_ELEMENTS_WITH_PROPERTY_COUNT_QUERY)
        .setParameter("pname", pname).setParameter("coll", collection).getSingleResult();
  }

  public long getElementsWithPropertyAndValueCount(String pname, String value, DigitalCollection coll) {
    return (Long) this.getEntityManager()
        .createNamedQuery(Constants.COLLECTION_ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY)
        .setParameter("pname", pname).setParameter("value", value).setParameter("coll", coll).getSingleResult();
  }

  public List<Element> getElementsWithPropertyAndValue(String pname, String value, DigitalCollection coll) {
    return this.getEntityManager()
        .createNamedQuery(Constants.COLLECTION_ELEMENTS_WITH_PROPERTY_AND_VALUE_SET_QUERY, Element.class)
        .setParameter("pname", pname).setParameter("value", value).setParameter("coll", coll).getResultList();
  }

  public long getDistinctPropertyValueCount(String pname, DigitalCollection collection) {
    return (Long) this.getEntityManager().createNamedQuery(Constants.COLLECTION_DISTINCT_PROPERTY_VALUE_COUNT_QUERY)
        .setParameter("pname", pname).setParameter("coll", collection).getSingleResult();
  }

  public List<String> getDistinctPropertyValueSet(String pname, DigitalCollection collection) {
    return this.getEntityManager()
        .createNamedQuery(Constants.COLLECTION_DISTINCT_PROPERTY_VALUES_SET_QUERY, String.class)
        .setParameter("pname", pname).setParameter("coll", collection).getResultList();
  }

  public long getValuesForElementCount(Element e) {
    return (Long) this.getEntityManager().createNamedQuery(Constants.VALUES_FOR_ELEMENT_COUNT)
        .setParameter("element", e).getSingleResult();
  }

  @SuppressWarnings("rawtypes")
  public List<Value> getValuesForElement(Element e) {
    return this.getEntityManager().createNamedQuery(Constants.VALUES_FOR_ELEMENT, Value.class)
        .setParameter("element", e).getResultList();
  }

  @SuppressWarnings("unchecked")
  public List<Object[]> getMostOccurringProperties(int limit, DigitalCollection collection) {
    return this.getEntityManager().createNamedQuery(Constants.COLLECTION_MOST_OCCURRING_PROPERTIES)
        .setParameter("coll", collection).setMaxResults(limit).getResultList();
  }

  public long getSumOfNumericProperty(String pname, DigitalCollection collection) {
    return (Long) this.getEntityManager().createNamedQuery(Constants.COLLECTION_SUM_VALUES_FOR_PROPERTY)
        .setParameter("pname", pname).setParameter("coll", collection).getSingleResult();
  }

  public double getAverageOfNumericProperty(String pname, DigitalCollection collection) {
    return (Double) this.getEntityManager().createNamedQuery(Constants.COLLECTION_AVG_VALUES_FOR_PROPERTY)
        .setParameter("pname", pname).setParameter("coll", collection).getSingleResult();
  }

  public long getMinOfNumericProperty(String pname, DigitalCollection collection) {
    return (Long) this.getEntityManager().createNamedQuery(Constants.COLLECTION_MIN_VALUE_FOR_PROPERTY)
        .setParameter("pname", pname).setParameter("coll", collection).getSingleResult();
  }

  public long getMaxOfNumericProperty(String pname, DigitalCollection collection) {
    return (Long) this.getEntityManager().createNamedQuery(Constants.COLLECTION_MAX_VALUE_FOR_PROPERTY)
        .setParameter("pname", pname).setParameter("coll", collection).getSingleResult();
  }
  
  public void getSDOfNumericProperty(String pname, DigitalCollection collection) {
    
  }

  @SuppressWarnings("unchecked")
  public List<Object[]> getValuesDistribution(DigitalCollection collection) {
    return this.getEntityManager().createNamedQuery(Constants.COLLECTION_VALUES_DISTRIBUTION)
        .setParameter("coll", collection).getResultList();
  }

  @SuppressWarnings("unchecked")
  public List<Object[]> getSpecificPropertyValuesDistribution(String pname, DigitalCollection collection) {
    return this.getEntityManager().createNamedQuery(Constants.COLLECTION_SPECIFIC_VALUE_DISTRIBUTION)
        .setParameter("pname", pname).setParameter("coll", collection).getResultList();
  }

  @SuppressWarnings("unchecked")
  public List<Object[]> getSpecificPropertyValuesDistributionInSet(String pname, List<Element> elmnts) {
    return this.getEntityManager().createNamedQuery(Constants.SPECIFIC_VALUE_DISTRIBUTION_IN_SET)
        .setParameter("pname", pname).setParameter("set", elmnts).getResultList();
  }

  public List<String> getDistinctValuesWithinFiltering(String pname1, String pname2, String value,
      DigitalCollection coll) {
    return this.getEntityManager().createNamedQuery("getDistinctValuesWithinPropertyFilteredCollection", String.class)
        .setParameter("pname1", pname1).setParameter("value", value).setParameter("pname2", pname2)
        .setParameter("coll", coll).getResultList();

  }

  public List<Element> getElementsWithinDoubleFilteredCollection(String pname1, String value1, String pname2,
      String value2, DigitalCollection coll) {
    return this.getEntityManager()
        .createNamedQuery(Constants.COLLECTION_ELEMENTS_WITHIN_DOUBLE_FILTER_SET_QUERY, Element.class)
        .setParameter("pname1", pname1).setParameter("value1", value1).setParameter("pname2", pname2)
        .setParameter("value2", value2).setParameter("coll", coll).getResultList();
  }

  public List<DigitalCollection> getAllCollections() {
    return this.getEntityManager().createNamedQuery(Constants.ALL_COLLECTIONS, DigitalCollection.class).getResultList();
  }

  public List<String> getAllCollectionNames() {
    return this.getEntityManager().createNamedQuery(Constants.ALL_COLLECTION_NAMES, String.class).getResultList();
  }

  public DigitalCollection getCollectionByName(String name) {
    return this.getEntityManager().createNamedQuery(Constants.COLLECTION_BY_NAME, DigitalCollection.class)
        .setParameter("name", name).getSingleResult();
  }

  public EntityManager getEntityManager() {
    return em;
  }

  public void setEntityManager(EntityManager em) {
    this.em = em;
  }
}
