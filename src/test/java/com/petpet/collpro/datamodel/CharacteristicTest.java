package com.petpet.collpro.datamodel;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    public void shouldPersistCharacteristic() throws Exception {
        StringCharacteristic sc = new StringCharacteristic("Test", "TestValue");
        em.getTransaction().begin();
        em.persist(sc);
        em.getTransaction().commit();

        List<Characteristic<?>> list = em.createQuery("SELECT c FROM CHARACTERISTIC c").getResultList();

        Assert.assertEquals(1, list.size());
        Assert.assertEquals("TestValue", list.get(0).getValue());

    }

    @Test
    public void shouldDeleteCharacteristic() throws Exception {
        em.getTransaction().begin();
        int update = em.createQuery("DELETE FROM CHARACTERISTIC c WHERE c.value like :value").setParameter("value", "TestValue").executeUpdate();
        em.getTransaction().commit();
        List<Characteristic<?>> list = em.createQuery("SELECT c FROM CHARACTERISTIC c").getResultList();

        Assert.assertEquals(1, update);
        Assert.assertEquals(0, list.size());
    }

}
