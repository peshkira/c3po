package com.petpet.collpro.datamodel;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.db.DBManager;

import javax.persistence.Query;

import junit.framework.Assert;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class ElementTest {
    
    private static DigitalCollection coll;
    
    @BeforeClass
    public static void beforeTests() {
        coll = new DigitalCollection("Test");
        DBManager.getInstance().persist(coll);
    }
    
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
        e.setUid(path);
        e.setCollection(coll);
        coll.getElements().add(e);
        
        DBManager db = DBManager.getInstance();
        db.persist(e);
        
        Element element = db.getEntityManager().find(Element.class, 1L);
        Assert.assertNotNull(element);
        Assert.assertEquals(name, element.getName());
        Assert.assertEquals(path, element.getUid());
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
        DigitalCollection coll = new DigitalCollection("TestMe");
        DBManager.getInstance().persist(coll);
        
        Element e1 = new Element();
        e1.setName("Element1");
        e1.setUid("path/to/file");
        e1.setCollection(coll);
        
        Element e2 = new Element();
        e2.setName("Element2");
        e2.setUid("path/to/other");
        e2.setCollection(coll);
        
        Element e3 = new Element();
        e3.setName("Element3");
        e3.setUid("path/file");
        e3.setCollection(coll);

        coll.getElements().add(e1);
        coll.getElements().add(e2);
        coll.getElements().add(e3);

        DBManager.getInstance().persist(e1);
        DBManager.getInstance().persist(e2);
        DBManager.getInstance().persist(e3);
        
        Query query = DBManager.getInstance().getEntityManager().createNamedQuery(Constants.COLLECTION_ELEMENTS_COUNT_QUERY).setParameter("coll", coll);
        Long count = (Long) query.getSingleResult();
        
        Assert.assertEquals(new Long(3), count);
    }
}
