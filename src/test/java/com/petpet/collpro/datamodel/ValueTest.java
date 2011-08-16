package com.petpet.collpro.datamodel;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.RollbackException;

import org.junit.After;
import org.junit.Test;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.db.DBManager;

import junit.framework.Assert;

public class ValueTest {
    
    @After
    public void after() {
        // clean persistence context
        // because of the rollback in the test
        // shouldNotStoreNullValues
        DBManager.getInstance().getEntityManager().clear();
    }
    
    @Test
    public void shouldStoreStringValue() throws Exception {
        String name = "Test";
        Date date = new Date();
        
        Value v = new StringValue();
        v.setMeasuredAt(date.getTime());
        v.setReliability(100);
        v.setValue(name);
        
        DBManager db = DBManager.getInstance();
        db.persist(v);
        db.getEntityManager().clear();
        
        Value value = db.getEntityManager().find(Value.class, 1L);
        Assert.assertNotNull(value);
        Assert.assertEquals(date.getTime(), value.getMeasuredAt());
        Assert.assertEquals(100, value.getReliability());
    }
    
    @Test
    public void shouldUpdateValue() throws Exception {
        DBManager db = DBManager.getInstance();
        String updated = "updated";
        
        Value value = db.getEntityManager().find(Value.class, 1L);
        value.setValue(updated);
        db.persist(value);
        db.getEntityManager().clear();
        
        value = db.getEntityManager().find(Value.class, 1L);
        Assert.assertEquals(updated, value.getValue());
    }
    
    @Test
    public void shouldDeleteValue() throws Exception {
        DBManager db = DBManager.getInstance();
        Value value = db.getEntityManager().find(Value.class, 1L);
        db.remove(value);
        db.getEntityManager().clear();
        
        value = db.getEntityManager().find(Value.class, 1L);
        Assert.assertNull(value);
        
    }
    
    @Test(expected = RollbackException.class)
    public void shouldNotStoreNullValue() throws Exception {
        Value v = new StringValue();
        DBManager.getInstance().persist(v);
    }
    
    @Test
    public void shouldTestNamedQueries() throws Exception {

        Element e = new Element();
        
        Query query = DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY).setParameter("pname", "mimetype").setParameter(
            "value", "application/pdf");
        Long count = (Long) query.getSingleResult();
        System.out.println("PDFs: " + count);
        
        query = DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.DISTINCT_PROPERTY_VALUE_COUNT_QUERY).setParameter("pname", "mimetype");
        count = (Long) query.getSingleResult();
        System.out.println("Distinct mimetypes: " + count);
        
        query = DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.DISTINCT_PROPERTY_VALUES_SET_QUERY).setParameter("pname", "mimetype");
        List<String> list = (List<String>) query.getResultList();
        for (String v : list) {
            System.out.println("mimetype: " + v);
        }
    }
}
