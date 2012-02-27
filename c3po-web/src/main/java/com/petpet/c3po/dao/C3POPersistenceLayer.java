package com.petpet.c3po.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.petpet.c3po.api.dao.GenericDAO;
import com.petpet.c3po.api.dao.PersistenceLayer;

@Stateless
public class C3POPersistenceLayer implements PersistenceLayer {
  
  private Map<Class, GenericDAO> daoMap = new HashMap<Class, GenericDAO>();
  
  @PersistenceContext
  private EntityManager em;

  private GenericDAO getDAO(Class clazz) {
    if (!daoMap.containsKey(clazz)) {
      GenericDAO realDao = new AbstractJPADAO(clazz);
      daoMap.put(clazz, realDao);
    }

    return daoMap.get(clazz);
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
  public List handleFindAll(Class<?> clazz) {
    return this.getDAO(clazz).findAll();
  }

  @Override
  public EntityManager getEntityManager() {
    return this.em;
  }

}
