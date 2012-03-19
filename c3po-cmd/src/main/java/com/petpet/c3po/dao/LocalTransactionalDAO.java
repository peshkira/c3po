package com.petpet.c3po.dao;

import java.util.Iterator;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.GenericDAO;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Value;
import com.petpet.c3po.utils.DBHelper;
import com.petpet.c3po.utils.Helper;

public class LocalTransactionalDAO implements GenericDAO<Object> {

  private static final Logger LOG = LoggerFactory.getLogger(LocalTransactionalDAO.class);

  private GenericJPADAO jpaDao;

  public LocalTransactionalDAO(GenericJPADAO jpaDao) {
    this.jpaDao = jpaDao;
  }

  @Override
  public Object persist(Object item) {
    LOG.trace("Trying to persist {}", item);
    try {
      this.jpaDao.getEntityManager().getTransaction().begin();
      Object result = this.jpaDao.persist(item);
      this.jpaDao.getEntityManager().getTransaction().commit();

      return result;

    } catch (ConstraintViolationException e) {
      return this.handleConstraintViolation(item, e.getMessage());

    } catch (PersistenceException e) {
      LOG.trace("caught persistence exception: {}", e.getCause().getMessage());
      if (e.getCause() instanceof ConstraintViolationException) {
        LOG.trace("in second instanceof: {}", e.getCause());
        return this.handleConstraintViolation(item, e.getCause().getMessage());
      } else {
        return this.handleError(e, "persisted", item);
      }

    } catch (Exception e) {
      return this.handleError(e, "persisted", item);

    }
  }

  @Override
  public Object update(Object item) {
    LOG.trace("Trying to update {}", item);
    try {
      this.jpaDao.getEntityManager().getTransaction().begin();
      Object result = this.jpaDao.update(item);
      this.jpaDao.getEntityManager().getTransaction().commit();
      return result;

    } catch (ConstraintViolationException e) {
      return this.handleConstraintViolation(item, e.getMessage());

    } catch (Exception e) {
      return this.handleError(e, "updated", item);
    }
  }

  @Override
  public void delete(Object item) {
    try {
      this.jpaDao.getEntityManager().getTransaction().begin();
      this.jpaDao.delete(item);
      this.jpaDao.getEntityManager().getTransaction().commit();

    } catch (Exception e) {
      this.handleError(e, "deleted", item);
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

  private Object handleConstraintViolation(Object item, String message) {
    LOG.error("c3po caught an error: {}", message);
    this.rollback();
    boolean retry = false;

    if (item instanceof Element) {
      Element elmnt = (Element) item;
      int before = elmnt.getValues().size();
      LOG.trace("Values before handling {}", before);
      Iterator<Value<?>> iterator = elmnt.getValues().iterator();

      while (iterator.hasNext()) {
        Value<?> v = iterator.next();

        if (v.getTypedValue() == null) {
          LOG.trace("Found null value for {}", v.getValue());
          iterator.remove();
        }
      }

      int after = elmnt.getValues().size();
      LOG.trace("Values after handling {}", after);
      retry = before > after;
    }

    if (retry) {
      LOG.debug("c3po will retry to store the element without the conflicting values...");

      Element elmnt = (Element) item;
      Element recreated = this.recreateElement(elmnt);
      item = this.persist(recreated);

      return item;
    }

    LOG.debug("c3po was unable to fix the error, skipping element {}", item);
    return null;
  }

  private Element recreateElement(Element elmnt) {
    LOG.trace("recreate element {}", elmnt);
    Element e = new Element(elmnt.getName(), elmnt.getUid());
    e.setCollection(elmnt.getCollection());

    for (Value<?> v : elmnt.getValues()) {
      Value<?> value = Helper.getTypedValue(v.getClass(), v.getValue());
      if (value != null && value.getValue() != null && value.getTypedValue() != null) {
        value.setProperty(Helper.getPropertyByName(v.getProperty().getName()));
        value.setSource(DBHelper.getValueSource(v.getSource().getName(), v.getSource().getVersion()));
        value.setStatus(v.getStatus());
        e.addValue(value);
      } else {
        LOG.error("Value is null: {}", v.getValue());
      }
    }

    return e;
  }

  private Object handleError(Exception e, String action, Object item) {
    LOG.error("c3po caught an error: {}, object {} could not be " + action, e.getMessage(), item);
    this.rollback();
    this.handleElement(item);

    return null;
  }

  private void rollback() {
    if (this.jpaDao.getEntityManager().getTransaction() != null
        && this.jpaDao.getEntityManager().getTransaction().isActive()) {
      LOG.warn("Transaction is still active, rolling it back manually");
      this.jpaDao.getEntityManager().getTransaction().rollback();

      this.flush();
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
