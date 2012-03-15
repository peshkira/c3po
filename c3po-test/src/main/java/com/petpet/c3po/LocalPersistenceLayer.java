package com.petpet.c3po;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.petpet.c3po.api.dao.GenericDAO;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.dao.AbstractJPADAO;
import com.petpet.c3po.dao.GenericJPADAO;

public class LocalPersistenceLayer implements PersistenceLayer {

  private Map<Class, LocalTransactionalDAO> daoMap = new HashMap<Class, LocalTransactionalDAO>();
  private EntityManagerFactory emf;
  private EntityManager em;

  public LocalPersistenceLayer() {
    this.emf = Persistence.createEntityManagerFactory("LocalC3POPersistenceUnit");
    this.em = emf.createEntityManager();
  }

  private GenericDAO getDAO(Class clazz) {
    GenericDAO dao;

    if (!this.daoMap.containsKey(clazz)) {
      AbstractJPADAO realDao = new AbstractJPADAO(clazz, this.em);
      this.daoMap.put(clazz, new LocalTransactionalDAO((GenericJPADAO) realDao));
    }

    return dao = this.daoMap.get(clazz);
  }

  @Override
  public Object handleCreate(Class<?> clazz, Object o) {
    return this.getDAO(clazz).persist(o);
  }

  @Override
  public Object handleUpdate(Class<?> clazz, Object o) {
    return this.getDAO(clazz).update(o);
  }

  @Override
  public void handleDelete(Class<?> clazz, Object o) {
    this.getDAO(clazz).delete(o);
  }

  @Override
  public Object handleFindById(Class<?> clazz, Object id) {
    return this.getDAO(clazz).findById((Long) id);
  }

  @Override
  public List<?> handleFindAll(Class<?> clazz) {
    return this.getDAO(clazz).findAll();
  }

  @Override
  public EntityManager getEntityManager() {
    return this.em;
  }

  @Override
  public synchronized void recover() {
    this.em.close();
    this.em = this.emf.createEntityManager();
    this.daoMap.clear();
  }

}
