package com.petpet.c3po;

import java.util.List;

import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.GenericDAO;
import com.petpet.c3po.dao.GenericJPADAO;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Value;

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
      this.handleError(e.getMessage(), this.jpaDao.getEntityManager().getTransaction(), item);
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
      LOG.error("An error occurred: {}, object {} could not be updated", e.getMessage(), item);
      this.handleError(e.getMessage(), this.jpaDao.getEntityManager().getTransaction(), item);
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
      LOG.error("An error occurred: {}, object could not be deleted", e.getMessage());
      this.handleError(e.getMessage(), this.jpaDao.getEntityManager().getTransaction(), item);
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

  private void handleError(String message, EntityTransaction transaction, Object item) {
    LOG.error("An error occurred: {}, object {} could not be persisted", message, item);
    if (this.jpaDao.getEntityManager().getTransaction() != null
        && this.jpaDao.getEntityManager().getTransaction().isActive()) {
      LOG.warn("Transaction is still active, rolling it back manually");
      this.jpaDao.getEntityManager().getTransaction().rollback();
    }

    this.jpaDao.getEntityManager().remove(item);

    LOG.warn("Trying to flush the entity manager");
    this.jpaDao.getEntityManager().getTransaction().begin();
    this.jpaDao.getEntityManager().flush();
    this.jpaDao.getEntityManager().getTransaction().commit();
  }

}
