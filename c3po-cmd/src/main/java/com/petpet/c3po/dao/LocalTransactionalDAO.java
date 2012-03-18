package com.petpet.c3po.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.GenericDAO;
import com.petpet.c3po.datamodel.DigitalCollection;
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

    } catch (ConstraintViolationException e) {
      return this.handleConstraintViolation(item, e.getConstraintViolations(), e.getMessage());

    } catch (Exception e) {
      return this.handleError(e.getMessage(), "persisted", item);

    }
  }

  @Override
  public Object update(Object item) {
    try {
      this.jpaDao.getEntityManager().getTransaction().begin();
      Object result = this.jpaDao.update(item);
      this.jpaDao.getEntityManager().getTransaction().commit();
      return result;

    } catch (ConstraintViolationException e) {
      return this.handleConstraintViolation(item, e.getConstraintViolations(), e.getMessage());

    } catch (Exception e) {
      return this.handleError(e.getMessage(), "updated", item);
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

  private Object handleConstraintViolation(Object item, Set<ConstraintViolation<?>> violations, String message) {
    LOG.error("c3po caught an error: {}", message);
    this.rollback();

    boolean retry = false;

    if (item instanceof Element) {
      Element e = (Element) item;
      int before = e.getValues().size();
      LOG.trace("Values before handling {}", before);
      Iterator<Value<?>> iterator = e.getValues().iterator();

      while (iterator.hasNext()) {
        Value<?> v = iterator.next();

        if (v.getTypedValue() == null) {
          LOG.trace("Found null value for {}", v.getValue());
          iterator.remove();
        }
      }

      int after = e.getValues().size();
      LOG.trace("Values after handling {}", after);
      retry = before > after;
    }

    if (retry) {
      LOG.debug("c3po will retry to store the element without the conflicting values...");
      Element e = (Element) item;
      e.setId(e.getId() - 1); // need to reset due to rollback
      item = this.update(e);
      this.flush();

      return item;
    }

    // cannot fix, return null
    return null;
  }

  private Object handleError(String message, String action, Object item) {
    LOG.error("c3po caught an error: {}, object {} could not be " + action, message, item);
    this.rollback();
    this.handleElement(item);
    this.flush();

    return null;
  }

  private void rollback() {
    if (this.jpaDao.getEntityManager().getTransaction() != null
        && this.jpaDao.getEntityManager().getTransaction().isActive()) {
      LOG.warn("Transaction is still active, rolling it back manually");
      this.jpaDao.getEntityManager().getTransaction().rollback();
    }
  }

  private void flush() {
    LOG.info("Flushing session");
    this.jpaDao.getEntityManager().getTransaction().begin();
    this.jpaDao.getEntityManager().flush();
    this.jpaDao.getEntityManager().getTransaction().commit();

    DBHelper.refreshProperties();
  }

  private void handleElement(Object item) {
    if (item instanceof Element) {
      Element e = (Element) item;
      LOG.trace("Removing corrupted element {}", e);
      this.jpaDao.getEntityManager().remove(e);
    }
  }

}
