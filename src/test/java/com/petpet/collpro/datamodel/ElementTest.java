package com.petpet.collpro.datamodel;

import javax.persistence.Query;

import org.junit.After;
import org.junit.Test;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.db.DBManager;

import junit.framework.Assert;

public class ElementTest {
    
    @After
    public void after() {
        // clean persistence context
        DBManager.getInstance().getEntityManager().clear();
    }
    
    @Test
    public void shouldStoreElement() throws Exception {
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
    public void shouldUpdateElement() throws Exception {
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
    
    @Test
    public void shouldCountElements() throws Exception {
        Element e1 = new Element();
        e1.setName("Element1");
        e1.setPath("path/to/file");
        
        Element e2 = new Element();
        e2.setName("Element2");
        e2.setPath("path/to/other");
        
        Element e3 = new Element();
        e3.setName("Element3");
        e3.setPath("path/file");
        
        DBManager.getInstance().persist(e1);
        DBManager.getInstance().persist(e2);
        DBManager.getInstance().persist(e3);
        
        Query query = DBManager.getInstance().getEntityManager().createNamedQuery(Constants.ELEMENTS_COUNT_QUERY);
        Long count = (Long) query.getSingleResult();
        
        Assert.assertEquals(new Long(3), count);
    }
}
