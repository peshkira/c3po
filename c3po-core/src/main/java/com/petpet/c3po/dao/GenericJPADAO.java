package com.petpet.c3po.dao;

import javax.persistence.EntityManager;

import com.petpet.c3po.api.dao.GenericDAO;

public interface GenericJPADAO<TYPE> extends GenericDAO<TYPE> {
  public EntityManager getEntityManager();

  public void setEntityManager(EntityManager em);

}
