package com.petpet.c3po.utils;

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
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

public class TestPersistenceLayer implements PersistenceLayer {

  private static final Logger LOG = LoggerFactory.getLogger(TestPersistenceLayer.class);

  @Override
  public void clearCache() {
    LOG.debug("clearCache()");
  }

  @Override
  public void close() throws C3POPersistenceException {
    LOG.debug("close()");
  }

  @Override
  public <T extends Model> long count(Class<T> clazz, Filter filter) {
    LOG.debug("count()");
    return 0;
  }

  @Override
  public void establishConnection(Map<String, String> config) throws C3POPersistenceException {
    LOG.debug("establishConnection()");
  }

  @Override
  public <T extends Model> Iterator<T> find(Class<T> clazz, Filter filter) {
    LOG.debug("find()");
    return null;
  }

  @Override
  public <T extends Model> List<String> distinct(Class<T> clazz, String f, Filter filter) {
    LOG.debug("distinct()");
    return null;
  }

  @Override
  public Cache getCache() {
    LOG.debug("getCache()");
    return null;
  }

  @Override
  public NumericStatistics getNumericStatistics(Property p, Filter filter) throws UnsupportedOperationException,
      IllegalArgumentException {

    LOG.debug("getNumericStatistics()");
    return null;
  }

  @Override
  public <T extends Model> Map<String, Long> getValueHistogramFor(Property p, Filter filter)
      throws UnsupportedOperationException {

    LOG.debug("getValueHistogram()");

    return null;
  }

  @Override
  public <T extends Model> void insert(T object) {
    LOG.debug("insert()");

  }

  @Override
  public boolean isConnected() {
    LOG.debug("isConnected()");
    return true;
  }

  @Override
  public <T extends Model> void remove(Class<T> clazz, Filter filter) {
    LOG.debug("remove()");

  }

  @Override
  public <T extends Model> void remove(T object) {
    LOG.debug("remove()");

  }

  @Override
  public void setCache(Cache c) {
    LOG.debug("setCache()");
  }

  @Override
  public <T extends Model> void update(T object) {
    LOG.debug("update()");
  }

  @Override
  public DB connect(Map<Object, Object> config) {
    LOG.debug("connect()");
    return null;
  }

  @Override
  public long count(String collection) {
    LOG.debug("count()");
    return 0;
  }

  @Override
  public long count(String collection, DBObject query) {
    LOG.debug("count()");
    return 0;
  }

  @Override
  public List distinct(String collection, String key) {
    LOG.debug("distinct()");
    return null;
  }

  @Override
  public List distinct(String collection, String key, DBObject query) {
    LOG.debug("distinct()");
    return null;
  }

  @Override
  public DBCursor find(String collection, DBObject ref) {
    LOG.debug("find()");
    return null;
  }

  @Override
  public DBCursor find(String collection, DBObject ref, DBObject keys) {
    LOG.debug("find()");
    return null;
  }

  @Override
  public DBCursor findAll(String collection) {
    LOG.debug("findAll()");
    return null;
  }

  @Override
  public DB getDB() {
    LOG.debug("getDB()");
    return null;
  }

  @Override
  public void insert(String collection, DBObject data) {
    LOG.debug("insert()");

  }

  @Override
  public MapReduceOutput mapreduce(String collection, MapReduceCommand cmd) {
    LOG.debug("mapreduce()");
    return null;
  }

}
