package com.petpet.c3po;

import java.util.List;

import javax.persistence.EntityManager;

import com.petpet.c3po.api.dao.GenericDAO;
import com.petpet.c3po.dao.GenericJPADAO;

public class LocalTransactionalDAO implements GenericDAO<Object> {
  
  private EntityManager em;

  private GenericJPADAO jpaDao;

  public LocalTransactionalDAO(EntityManager em, GenericJPADAO jpaDao) {
      this.em = em;
      this.jpaDao = jpaDao.setEntityManager(em);
  }


  @Override
  public Object persist(Object item) {
    this.em.getTransaction().begin();
    Object result = this.jpaDao.persist(item);
    this.em.getTransaction().commit();
    return result;
  }

  @Override
  public Object update(Object item) {
    this.em.getTransaction().begin();
    Object result = this.jpaDao.update(item);
    this.em.getTransaction().commit();
    return result;
  }

  @Override
  public void delete(Object item) {
      this.em.getTransaction().begin();
      this.jpaDao.delete(item);
      this.em.getTransaction().commit();
  }

  @Override
  public Object findById(Long id) {
    return this.jpaDao.findById(id);
  }

  @Override
  public List<Object> findAll() {
    return this.jpaDao.findAll();
  }

}
