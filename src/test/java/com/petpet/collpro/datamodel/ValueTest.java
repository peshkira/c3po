package com.petpet.collpro.datamodel;

import java.util.Date;

import org.junit.Test;

import com.petpet.collpro.db.DBManager;

import junit.framework.Assert;

public class ValueTest {
    
    @Test
    public void shouldStoreSimpleValue() throws Exception {
        String name = "Test";
        Date date = new Date();
        
        Value v = new StringValue();
        v.setMeasuredAt(date.getTime());
        v.setReliability(100);
        
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
        db.persist(value);
        db.getEntityManager().clear();
        
        value = db.getEntityManager().find(Value.class, 1L);
        //TODO assert
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
    
    @Test
    public void shouldStoreGenericValue() throws Exception {
        String name = "Test";
        Date date = new Date();
        
        NumericValue v = new NumericValue();
        v.setMeasuredAt(date.getTime());
        v.setReliability(100);
        v.setValue(100L);
        DBManager db = DBManager.getInstance();
        db.persist(v);
        db.getEntityManager().clear();
     
        Value<Long> value = db.getEntityManager().find(Value.class, 2L);
        Assert.assertNotNull(value);
        Assert.assertEquals(date.getTime(), value.getMeasuredAt());
        Assert.assertEquals(100, value.getReliability());
        Assert.assertNull(value.getValue());
    }
}
