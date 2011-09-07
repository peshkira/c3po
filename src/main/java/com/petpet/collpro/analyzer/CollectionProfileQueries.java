package com.petpet.collpro.analyzer;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.datamodel.Value;
import com.petpet.collpro.db.DBManager;

import java.util.List;

public class CollectionProfileQueries {

    public List<Property> getAllProperties() {
        return DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.ALL_PROPERTIES_QUERY, Property.class).getResultList();
    }

    public List<Property> getAllPropertiesInCollection(DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.ALL_COLLECTION_PROPERTIES_QUERY, Property.class)
                .setParameter("coll", collection).getResultList();
    }

    public List<Value> getValuesByPropertyName(String pname, DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.COLLECTION_VALUES_BY_PROPERTY_NAME_QUERY, Value.class)
                .setParameter("pname", pname).setParameter("coll", collection).getResultList();
    }

    public List<Value> getValueByPropertyNameAndValue(String pname, String value) {
        return DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.VALUES_BY_NAME_AND_VALUE, Value.class).setParameter("pname", pname)
                .setParameter("value", value).getResultList();
    }

    public List<Value> getValueByPropertyNameAndValue(String pname, String value, DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.COLLECTION_VALUES_BY_NAME_AND_VALUE, Value.class)
                .setParameter("pname", pname).setParameter("value", value).setParameter("coll", collection)
                .getResultList();
    }

    public long getElementsCount(DigitalCollection collection) {
        return (Long) DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.COLLECTION_ELEMENTS_COUNT_QUERY).setParameter("coll", collection)
                .getSingleResult();
    }

    public long getElementsWithPropertyCount(String pname, DigitalCollection collection) {
        return (Long) DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.COLLECTION_ELEMENTS_WITH_PROPERTY_COUNT_QUERY).setParameter("pname", pname)
                .setParameter("coll", collection).getSingleResult();
    }

    public long getElementsWithPropertyAndValueCount(String pname, String value, DigitalCollection coll) {
        return (Long) DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.COLLECTION_ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY)
                .setParameter("pname", pname).setParameter("value", value).setParameter("coll", coll).getSingleResult();
    }

    public List<Element> getElementsWithPropertyAndValue(String pname, String value, DigitalCollection coll) {
        return DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.COLLECTION_ELEMENTS_WITH_PROPERTY_AND_VALUE_SET_QUERY, Element.class)
                .setParameter("pname", pname).setParameter("value", value).setParameter("coll", coll).getResultList();
    }

    public long getDistinctPropertyValueCount(String pname, DigitalCollection collection) {
        return (Long) DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.COLLECTION_DISTINCT_PROPERTY_VALUE_COUNT_QUERY)
                .setParameter("pname", pname).setParameter("coll", collection).getSingleResult();
    }

    public List<String> getDistinctPropertyValueSet(String pname, DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.COLLECTION_DISTINCT_PROPERTY_VALUES_SET_QUERY).setParameter("pname", pname)
                .setParameter("coll", collection).getResultList();
    }

    public long getValuesForElementCount(Element e) {
        return (Long) DBManager.getInstance().getEntityManager().createNamedQuery(Constants.VALUES_FOR_ELEMENT_COUNT)
                .setParameter("element", e).getSingleResult();
    }

    public List<Value> getValuesForElement(Element e) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.VALUES_FOR_ELEMENT, Value.class)
                .setParameter("element", e).getResultList();
    }

    public List<Object[]> getMostOccurringProperties(int limit, DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.COLLECTION_MOST_OCCURRING_PROPERTIES).setParameter("coll", collection)
                .setMaxResults(limit).getResultList();
    }

    public long getSumOfNumericProperty(String pname, DigitalCollection collection) {
        return (Long) DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.COLLECTION_SUM_VALUES_FOR_PROPERTY).setParameter("pname", pname)
                .setParameter("coll", collection).getSingleResult();
    }

    public double getAverageOfNumericProperty(String pname, DigitalCollection collection) {
        return (Double) DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.COLLECTION_AVG_VALUES_FOR_PROPERTY).setParameter("pname", pname)
                .setParameter("coll", collection).getSingleResult();
    }

    public List<Object[]> getValuesDistribution(DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager().createNamedQuery(Constants.COLLECTION_VALUES_DISTRIBUTION)
                .setParameter("coll", collection).getResultList();
    }

    public List<Object[]> getSpecificPropertyValuesDistribution(String pname, DigitalCollection collection) {
        return DBManager.getInstance().getEntityManager()
                .createNamedQuery(Constants.COLLECTION_SPECIFIC_VALUE_DISTRIBUTION).setParameter("pname", pname)
                .setParameter("coll", collection).getResultList();
    }

    public void getDoubleFilteredSortedCollection(String pname1, String pname2, DigitalCollection coll) {

        List<String> filter1 = this.getDistinctPropertyValueSet(pname1, coll);

        for (String f1 : filter1) {
            System.out.println(pname1 + ": " + f1);

            List<String> list = DBManager.getInstance().getEntityManager()
                    .createNamedQuery("getDistinctValuesWithinPropertyFilteredCollection")
                    .setParameter("pname1", pname1).setParameter("value", f1).setParameter("pname2", pname2)
                    .setParameter("coll", coll).getResultList();

            for (String s : list) {
                System.out.println("\t" + pname2 + ": " + s);

                List<Element> elements = DBManager
                        .getInstance()
                        .getEntityManager()
                        .createQuery(
                                "SELECT val.element FROM Value val WHERE val.property.name = :pname2 AND val.value = :value2 AND val.element IN (SELECT v.element FROM Value v WHERE v.property.name = :pname1 AND v.value = :value1 AND v.element.collection = :coll)",
                                Element.class).setParameter("pname1", pname1).setParameter("value1", f1)
                        .setParameter("pname2", pname2).setParameter("value2", s).setParameter("coll", coll)
                        .getResultList();

                for (Element e : elements) {
                    System.out.println("\t\t" + e.getName());
                }
            }
        }

    }
}
