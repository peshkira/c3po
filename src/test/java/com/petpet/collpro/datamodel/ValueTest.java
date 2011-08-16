package com.petpet.collpro.datamodel;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.RollbackException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.db.DBManager;

import junit.framework.Assert;

public class ValueTest {
    
    private static Element edummy;
    private static Property pdummy;
    private static ValueSource vdummy;
    
    @BeforeClass
    public static void beforeTests() {
        edummy = new Element("Dummy", "Dummy");
        pdummy = new Property("Dummy");
        vdummy = new ValueSource("Dummy");
        
        DBManager.getInstance().persist(edummy);
        DBManager.getInstance().persist(pdummy);
        DBManager.getInstance().persist(vdummy);
    }
    
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
        v.setElement(edummy);
        v.setProperty(pdummy);
        v.setSource(vdummy);
        
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
        
        Element e1 = new Element("e1", "path/to/file/e1");
        Element e2 = new Element("e2", "path/to/file/e2");
        Element e3 = new Element("e3", "path/to/file/e3");
        
        DBManager.getInstance().persist(e1);
        DBManager.getInstance().persist(e2);
        DBManager.getInstance().persist(e3);
        
        Property p1 = new Property("mimetype");
        DBManager.getInstance().persist(p1);
        
        ValueSource vs = new ValueSource("source");
        DBManager.getInstance().persist(vs);
        
        StringValue v1 = new StringValue("application/pdf");
        v1.setElement(e1);
        v1.setProperty(p1);
        v1.setSource(vs);
        v1.setMeasuredAt(new Date().getTime());
        
        StringValue v2 = new StringValue("document/txt");
        v2.setElement(e2);
        v2.setProperty(p1);
        v2.setSource(vs);
        v2.setMeasuredAt(new Date().getTime());
        
        StringValue v3 = new StringValue("application/pdf");
        v3.setElement(e2);
        v3.setProperty(p1);
        v3.setSource(vs);
        v3.setMeasuredAt(new Date().getTime());
        
        DBManager.getInstance().persist(v1);
        DBManager.getInstance().persist(v2);
        DBManager.getInstance().persist(v3);
        
        Query query = DBManager.getInstance().getEntityManager().createNamedQuery(Constants.VALUES_BY_PROPERTY_NAME_QUERY)
            .setParameter("name", "mimetype");
        List list = query.getResultList();
        Assert.assertEquals(3, list.size());
        
        query = DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY).setParameter("pname", "mimetype").setParameter(
            "value", "application/pdf");
        Long count = (Long) query.getSingleResult();
        Assert.assertEquals(new Long(2), count);
        
        query = DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.DISTINCT_PROPERTY_VALUE_COUNT_QUERY).setParameter("pname", "mimetype");
        count = (Long) query.getSingleResult();
        Assert.assertEquals(new Long(2), count);
        
        query = DBManager.getInstance().getEntityManager().createNamedQuery(
            Constants.DISTINCT_PROPERTY_VALUES_SET_QUERY).setParameter("pname", "mimetype");
        list = query.getResultList();
        
        Assert.assertEquals(new Long(2), count);
        Assert.assertEquals("application/pdf", list.get(0));
        Assert.assertEquals("document/txt", list.get(1));
        
    }
}
