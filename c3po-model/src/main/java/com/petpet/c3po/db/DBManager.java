package com.petpet.c3po.db;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public final class DBManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(DBManager.class);

  private static DBManager uniqueInstance;

  private EntityManagerFactory emf;

  private EntityManager em;

  public static synchronized DBManager getInstance() {
    if (DBManager.uniqueInstance == null) {
      DBManager.uniqueInstance = new DBManager();
      DBManager.uniqueInstance.createEntityManagerFactory();
      DBManager.LOGGER.info("DBManager created");
    }

    return DBManager.uniqueInstance;
  }

  public EntityManager getEntityManager() {
    if (this.em == null) {
      em = this.emf.createEntityManager();
    }

    return this.em;
  }

  public void createEntityManagerFactory() {
    this.emf = Persistence.createEntityManagerFactory("LocalC3POPersistenceUnit");
  }

  public void close() {
    if (this.em != null) {
      this.em.close();
      this.em = null;
    }

    if (this.emf != null) {
      this.emf.close();
    }
  }

  public synchronized void persist(Object o) {
    if (o != null) {
      LOGGER.info("Persisting... {}", o);
      EntityManager em = this.getEntityManager();
      em.getTransaction().begin();
      em.persist(o);
      em.getTransaction().commit();
    } else {
      LOGGER.warn("Cannot persist a null object reference, skipping");
    }
  }

  public synchronized void remove(Object o) {
    if (o != null) {
      EntityManager em = this.getEntityManager();
      em.getTransaction().begin();
      em.remove(o);
      em.getTransaction().commit();
    } else {
      LOGGER.warn("Cannot remove a null object reference, skipping");
    }
  }

  private DBManager() {

  }
}
