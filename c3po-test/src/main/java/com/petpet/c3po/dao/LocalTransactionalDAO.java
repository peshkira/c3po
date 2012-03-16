package com.petpet.c3po.dao;

import java.util.List;

import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.GenericDAO;
import com.petpet.c3po.dao.GenericJPADAO;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Value;
import com.petpet.c3po.utils.DBHelper;

public class LocalTransactionalDAO implements GenericDAO<Object> {

  private static final Logger LOG = LoggerFactory.getLogger(LocalTransactionalDAO.class);

  private GenericJPADAO jpaDao;

  public LocalTransactionalDAO(GenericJPADAO jpaDao) {
    this.jpaDao = jpaDao;
  }

  @Override
  public Object persist(Object item) {
    try {
      this.jpaDao.getEntityManager().getTransaction().begin();
      Object result = this.jpaDao.persist(item);
      this.jpaDao.getEntityManager().getTransaction().commit();
      return result;
    } catch (Exception e) {
      this.handleError(e.getMessage(), "persisted", item);
      return null;
    }
  }

  @Override
  public Object update(Object item) {
    try {
      this.jpaDao.getEntityManager().getTransaction().begin();
      Object result = this.jpaDao.update(item);
      this.jpaDao.getEntityManager().getTransaction().commit();
      return result;
    } catch (Exception e) {
      this.handleError(e.getMessage(), "updated", item);
      return null;
    }
  }

  @Override
  public void delete(Object item) {
    try {
      this.jpaDao.getEntityManager().getTransaction().begin();
      this.jpaDao.delete(item);
      this.jpaDao.getEntityManager().getTransaction().commit();
    } catch (Exception e) {
      this.handleError(e.getMessage(), "deleted", item);
    }
  }

  @Override
  public Object findById(Long id) {
    return this.jpaDao.findById(id);
  }

  @Override
  public List<Object> findAll() {
    return this.jpaDao.findAll();
  }

  private void handleError(String message, String action, Object item) {
    LOG.error("c3po caught an error: {}, object {} could not be" + item, message, action);
    if (this.jpaDao.getEntityManager().getTransaction() != null
        && this.jpaDao.getEntityManager().getTransaction().isActive()) {
      LOG.warn("Transaction is still active, rolling it back manually");
      this.jpaDao.getEntityManager().getTransaction().rollback();
    }

    LOG.info("Removing conflicting object");
    this.jpaDao.getEntityManager().remove(item);

    LOG.info("Recovering session");
    this.jpaDao.getEntityManager().getTransaction().begin();
    this.jpaDao.getEntityManager().flush();
    this.jpaDao.getEntityManager().getTransaction().commit();

    DBHelper.refreshProperties();
  }

}
