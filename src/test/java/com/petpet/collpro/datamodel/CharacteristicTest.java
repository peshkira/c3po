package com.petpet.collpro.datamodel;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class CharacteristicTest {

    private EntityManagerFactory emf;

    private EntityManager em;

    @Before
    public void createEntityManager() {
        emf = Persistence.createEntityManagerFactory("CollProTestPersistenceUnit");
        em = emf.createEntityManager();
    }

    @After
    public void closeEntityManager() {
        if (em != null)
        em.close();
    }

    @Test
    public void shouldPersistStringCharacteristic() throws Exception {
        StringCharacteristic sc = new StringCharacteristic("Test", "TestValue");
        em.getTransaction().begin();
        em.persist(sc);
        em.getTransaction().commit();
        
        List<StringCharacteristic> list = this.em.createQuery("select c from StringCharacteristic c", StringCharacteristic.class).getResultList();

        Assert.assertFalse(list.isEmpty());
        Assert.assertEquals("TestValue", list.get(0).getValue());

    }

    @Test
    public void shouldDeleteCharacteristic() throws Exception {
        StringCharacteristic sc = new StringCharacteristic("Test", "TestValue");
        this.em.getTransaction().begin();
        this.em.persist(sc);
        this.em.getTransaction().commit();
        
        List<StringCharacteristic> list = this.em.createQuery("select c from StringCharacteristic c", StringCharacteristic.class).getResultList();
        Assert.assertFalse(list.isEmpty());
        Assert.assertEquals("TestValue", list.get(0).getValue());
        
        this.em.getTransaction().begin();
        int update = this.em.createQuery("delete from StringCharacteristic c WHERE c.value like :value").setParameter("value", "TestValue").executeUpdate();
        this.em.getTransaction().commit();
        
        list = this.em.createQuery("select c from StringCharacteristic c", StringCharacteristic.class).getResultList();

        Assert.assertEquals(1, update);
        Assert.assertEquals(0, list.size());
    }

}
