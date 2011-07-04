package com.petpet.collpro.datamodel;
import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.petpet.collpro.db.DBManager;

import junit.framework.Assert;

import static org.junit.Assert.*;

public class ElementTest {
    
    @Before
    public void setup() {
        
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void shouldStoreElement() {
        String name = "Test";
        String path = "path/to/file";
        Element e = new Element();
        e.setName(name);
        e.setPath(path);
        
        DBManager db = DBManager.getInstance();
        db.persist(e);
     
        Element element = db.getEntityManager().find(Element.class, 1L);
        Assert.assertNotNull(element);
        Assert.assertEquals(name, element.getName());
        Assert.assertEquals(path, element.getPath());
    }
    
    @Test
    public void shouldUpdateElement() {
        DBManager db = DBManager.getInstance();
        Element element = db.getEntityManager().find(Element.class, 1L);
        Assert.assertNotNull(element);
        
        String name = "New";
        element.setName(name);
        db.persist(element);
        
        element = db.getEntityManager().find(Element.class, 1L);
        Assert.assertNotNull(element);
        Assert.assertEquals(name, element.getName());
    }
    
    @Test
    public void shouldDeleteElement() throws Exception {
        DBManager db = DBManager.getInstance();
        Element element = db.getEntityManager().find(Element.class, 1L);
        db.remove(element);
        element = db.getEntityManager().find(Element.class, 1L);
        Assert.assertNull(element);
    }
}
