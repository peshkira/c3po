package com.petpet.c3po.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Model;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

public class DefaultPersistenceLayer implements PersistenceLayer {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultPersistenceLayer.class);

  private PersistenceLayer persistence;

  public DefaultPersistenceLayer() {
    persistence = new MongoPersistenceLayer();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clearCache() {
    this.persistence.clearCache();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    try {
      this.persistence.close();
    } catch (C3POPersistenceException e) {
      LOG.error("An error occurred: {}", e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> long count(Class<T> clazz, Filter filter) {
    return this.persistence.count(clazz, filter);
  }

  @Override
  public void establishConnection(Map<String, String> config) throws C3POPersistenceException {
    this.persistence.establishConnection(config);

  }

  @Override
  public <T extends Model> Iterator<T> find(Class<T> clazz, Filter filter) {
    return this.persistence.find(clazz, filter);
  }

  @Override
  public <T extends Model> List<String> distinct(Class<T> clazz, Property p, Filter filter) {
    return this.persistence.distinct(clazz, p, filter);
  }

  @Override
  public Cache getCache() {
    return this.persistence.getCache();
  }

  @Override
  public <T extends Model> NumericStatistics getNumericStatistics(Class<T> clazz, Property p, Filter filter)
      throws UnsupportedOperationException {

    return this.persistence.getNumericStatistics(clazz, p, filter);
  }

  @Override
  public <T extends Model> Map<String, Integer> getValueHistogramFor(Class<T> clazz, Property p, Filter filter)
      throws UnsupportedOperationException {

    return this.persistence.getValueHistogramFor(clazz, p, filter);
  }

  @Override
  public <T extends Model> void insert(T object) {
    this.persistence.insert(object);
  }

  @Override
  public boolean isConnected() {
    return this.persistence.isConnected();
  }

  @Override
  public <T extends Model> void remove(T object) {
    this.persistence.remove(object);
  }

  @Override
  public <T extends Model> void remove(Class<T> clazz, Filter filter) {
    this.persistence.remove(clazz, filter);
  }

  public void setCache(Cache c) {
    this.persistence.setCache(c);
  }

  @Override
  public <T extends Model> void update(T object) {
    this.persistence.update(object);
  }

  // --- DEPRECATED METHODS REMOVE THEM

  @Override
  @Deprecated
  public DB getDB() {
    return this.persistence.getDB();
  }

  @Override
  @Deprecated
  public DB connect(Map<Object, Object> config) {
    return this.persistence.connect(config);
  }

  @Override
  @Deprecated
  public DBCursor findAll(String collection) {
    return this.persistence.findAll(collection);
  }

  @Override
  @Deprecated
  public DBCursor find(String collection, DBObject ref) {
    return this.persistence.find(collection, ref);
  }

  @Override
  @Deprecated
  public DBCursor find(String collection, DBObject ref, DBObject keys) {
    return this.persistence.find(collection, ref, keys);
  }

  @Override
  @Deprecated
  public List distinct(String collection, String key) {
    return this.persistence.distinct(collection, key);
  }

  @Override
  @Deprecated
  public List distinct(String collection, String key, DBObject query) {
    return this.persistence.distinct(collection, key, query);
  }

  @Override
  @Deprecated
  public void insert(String collection, DBObject data) {
    this.persistence.insert(collection, data);
  }

  @Override
  @Deprecated
  public long count(String collection) {
    return this.persistence.count(collection);
  }

  @Override
  public long count(String collection, DBObject query) {
    return this.persistence.count(collection, query);
  }

  @Override
  @Deprecated
  public MapReduceOutput mapreduce(String collection, MapReduceCommand cmd) {
    return this.persistence.mapreduce(collection, cmd);
  }

}
