package com.petpet.c3po;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.petpet.c3po.api.dao.GenericDAO;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.dao.AbstractJPADAO;
import com.petpet.c3po.dao.GenericJPADAO;

public class LocalPersistenceLayer implements PersistenceLayer {
  
  private Map<Class, GenericDAO> daoMap = new HashMap<Class, GenericDAO>();
  private EntityManagerFactory emf;
  private EntityManager em;
  
  public LocalPersistenceLayer(EntityManagerFactory emf) {
    this.emf = emf;
    this.em = emf.createEntityManager();
  }
  
  private GenericDAO getDAO(Class clazz) {
    GenericDAO dao;

    if(!daoMap.containsKey(clazz))
    {
        GenericDAO realDao = new AbstractJPADAO(clazz);
        daoMap.put(clazz, realDao);
    }
    
    dao = daoMap.get(clazz);
    
    return new LocalTransactionalDAO(em, (GenericJPADAO) dao); 
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

}
