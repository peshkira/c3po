package com.petpet.collpro.datamodel;
import org.junit.Test;

import com.petpet.collpro.db.DBManager;

import junit.framework.Assert;

public class ElementTest {
    
    @Test
    public void shouldStoreElement() {
        String name = "Test";
        String path = "path/to/file";
        Element e = new Element();
        e.setName(name);
        e.setPath(path);
        
        DBManager db = DBManager.getInstance();
        db.persist(e);
        db.getEntityManager().clear();
     
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
        db.getEntityManager().clear();
        
        element = db.getEntityManager().find(Element.class, 1L);
        Assert.assertNotNull(element);
        Assert.assertEquals(name, element.getName());
    }
    
    @Test
    public void shouldDeleteElement() throws Exception {
        DBManager db = DBManager.getInstance();
        Element element = db.getEntityManager().find(Element.class, 1L);
        db.remove(element);
        db.getEntityManager().clear();
        
        element = db.getEntityManager().find(Element.class, 1L);
        Assert.assertNull(element);
    }
}
