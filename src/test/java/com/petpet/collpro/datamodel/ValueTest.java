package com.petpet.collpro.datamodel;

import java.util.Date;

import javax.persistence.RollbackException;

import org.junit.Test;

import com.petpet.collpro.db.DBManager;

import junit.framework.Assert;

public class ValueTest {
    
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
    
    @Test(expected=RollbackException.class)
    public void shouldNotStoreNullValue() throws Exception {
       Value v = new StringValue();       
       DBManager.getInstance().persist(v);
    }
}
