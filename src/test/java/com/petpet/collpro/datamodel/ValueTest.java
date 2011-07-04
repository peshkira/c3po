package com.petpet.collpro.datamodel;

import java.util.Date;

import org.junit.Test;

import com.petpet.collpro.db.DBManager;

import junit.framework.Assert;

import static org.junit.Assert.*;

public class ValueTest {
    
    @Test
    public void shouldStoreSimpleValue() throws Exception {
        String name = "Test";
        Date date = new Date();
        
        Value v = new Value();
        v.setJsonValue(name);
        v.setMeasuredAt(date.getTime());
        v.setReliability(100);
        
        DBManager db = DBManager.getInstance();
        db.persist(v);
        db.getEntityManager().clear();
     
        Value value = db.getEntityManager().find(Value.class, 1L);
        Assert.assertNotNull(value);
        Assert.assertEquals(name, value.getJsonValue());
        Assert.assertEquals(date.getTime(), value.getMeasuredAt());
        Assert.assertEquals(100, value.getReliability());
    }
    
    @Test
    public void shouldUpdateValue() throws Exception {
        DBManager db = DBManager.getInstance();
        String updated = "updated";
        
        Value value = db.getEntityManager().find(Value.class, 1L);
        value.setJsonValue(updated);
        db.persist(value);
        db.getEntityManager().clear();
        
        value = db.getEntityManager().find(Value.class, 1L);
        Assert.assertEquals(updated, value.getJsonValue());
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
        
        Value<Integer> v = new Value<Integer>();
        v.setJsonValue(name);
        v.setMeasuredAt(date.getTime());
        v.setReliability(100);
        v.setValue(100);
        DBManager db = DBManager.getInstance();
        db.persist(v);
        db.getEntityManager().clear();
     
        Value<Integer> value = db.getEntityManager().find(Value.class, 2L);
        Assert.assertNotNull(value);
        Assert.assertEquals(name, value.getJsonValue());
        Assert.assertEquals(date.getTime(), value.getMeasuredAt());
        Assert.assertEquals(100, value.getReliability());
        Assert.assertNull(value.getValue());
    }
}
