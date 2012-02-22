package com.petpet.c3po.dao;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaQuery;

import com.petpet.c3po.api.dao.GenericDAO;

public class AbstractJPADAO<TYPE> implements GenericJPADAO<TYPE> {

  @PersistenceContext
  protected EntityManager em;

  protected Class entityClass;

  public Class getEntityClass() {
    return entityClass;
  }

  public void setEntityClass(Class entityClass) {
    this.entityClass = entityClass;
  }

  public AbstractJPADAO() {
    ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
    this.entityClass = (Class<TYPE>) genericSuperclass.getActualTypeArguments()[0];
  }

  public AbstractJPADAO(Class clazz) {
    this.entityClass = clazz;
  }

  public EntityManager getEntityManager() {
    return em;
  }

  public GenericJPADAO setEntityManager(EntityManager em) {
    this.em = em;
    return this;
  }

  @Override
  public TYPE persist(TYPE item) {
    if (item == null)
      throw new PersistenceException("Item may not be null");
    em.persist(item);
    return item;
  }

  @Override
  public TYPE update(TYPE item) {
    if (item == null)
      throw new PersistenceException("Item may not be null");

    em.merge(item);
    return item;
  }

  @Override
  public void delete(TYPE item) {
    if (item == null)
      throw new PersistenceException("Item may not be null");

    em.remove(em.merge(item));

  }

  @Override
  public TYPE findById(Long id) {
    if (id == null || id < 1)
      throw new PersistenceException("Id may not be null or negative");

    return (TYPE) em.find(entityClass, id);
  }

  @Override
  public List<TYPE> findAll() {
    CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
    cq.select(cq.from(entityClass));
    return em.createQuery(cq).getResultList();
  }

}
