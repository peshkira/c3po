package com.petpet.c3po.dao.mongo;

import static com.mongodb.MapReduceCommand.OutputType.MERGE;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.ActionLog;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Model;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.dao.DBCache;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

public class MongoPersistenceLayer implements PersistenceLayer {

  /**
   * A default logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(MongoPersistenceLayer.class);

  /**
   * The hostname of the server where the db is running.
   */
  private static final String CNF_DB_HOST = "db.host";

  /**
   * The port of the server where the db is listening to.
   */
  private static final String CNF_DB_PORT = "db.port";

  /**
   * The database name.
   */
  private static final String CNF_DB_NAME = "db.name";

  /**
   * The elements collection in the document store.
   */
  private static final String TBL_ELEMENTS = "elements";

  /**
   * The properties collection in the document store.
   */
  private static final String TBL_PROEPRTIES = "properties";

  /**
   * The source collection in the document store.
   */
  private static final String TBL_SOURCES = "sources";

  /**
   * The actions done on a collection basis in the db.
   */
  private static final String TBL_ACTIONLOGS = "actionlogs";

  /**
   * An internally managed table for numeric statistics. This table is managed
   * by this concrete implementation and is just for optimization purposes.
   */
  private static final String TBL_NUMERIC_STATISTICS = "numeric_statistics";

  /**
   * A constant used for the last filter object that might be cached.
   */
  private static final String LAST_FILTER = "constant.last_filter";

  /**
   * A constant used for the last filter query that might be cached.
   */
  private static final String LAST_FILTER_QUERY = "constant.last_filter.query";

  /**
   * A javascript Map function for calculating the min, max, sum, avg, sd and
   * var of a numeric property. Note that there is a wildcard @1 and wildcard @2
   * 
   * @1 = the id under which the results will be output. <br>
   * @2 = the key of the desired numeric property prior to usage.
   */
  public static final String AGGREGATE_MAP = "function map() {emit(@1,{sum: this.metadata['@2'].value, min: this.metadata['@2'].value,max: this.metadata['@2'].value,count:1,diff: 0,});}";

  /**
   * The reduce of the aggregation functions.
   */
  public static final String AGGREGATE_REDUCE = "function reduce(key, values) {var a = values[0];for (var i=1; i < values.length; i++){var b = values[i];var delta = a.sum/a.count - b.sum/b.count;var weight = (a.count * b.count)/(a.count + b.count);a.diff += b.diff + delta*delta*weight;a.sum += b.sum;a.count += b.count;a.min = Math.min(a.min, b.min);a.max = Math.max(a.max, b.max);}return a;}";

  /**
   * A finalize function for the aggregation map reduce job, to calculate the
   * average, standard deviation and variance.
   */
  public static final String AGGREGATE_FINALIZE = "function finalize(key, value){ value.avg = value.sum / value.count;value.variance = value.diff / value.count;value.stddev = Math.sqrt(value.variance);return value;}";

  private Mongo mongo;

  private DB db;

  private Cache dbCache;

  private boolean connected;

  private Map<String, ModelDeserializer> deserializers;

  private Map<String, ModelSerializer> serializers;

  private Map<String, DBCollection> collections;

  private MongoFilterSerializer filterSerializer;

  public MongoPersistenceLayer() {
    this.deserializers = new HashMap<String, ModelDeserializer>();
    this.deserializers.put(Element.class.getName(), new ElementDeserialzer(this));
    this.deserializers.put(Property.class.getName(), new PropertyDeserialzer());
    this.deserializers.put(Source.class.getName(), new SourceDeserializer());
    this.deserializers.put(ActionLog.class.getName(), new ActionLogDeserializer());

    this.serializers = new HashMap<String, ModelSerializer>();
    this.serializers.put(Element.class.getName(), new ElementSerializer());
    this.serializers.put(Property.class.getName(), new PropertySerializer());
    this.serializers.put(Source.class.getName(), new SourceSerializer());
    this.serializers.put(ActionLog.class.getName(), new ActionLogSerializer());

    this.filterSerializer = new MongoFilterSerializer();

    this.collections = new HashMap<String, DBCollection>();

  }

  @Override
  public void establishConnection(Map<String, String> config) throws C3POPersistenceException {
    this.close();

    try {
      String name = config.get(CNF_DB_NAME);
      String host = config.get(CNF_DB_HOST);
      int port = Integer.parseInt(config.get(CNF_DB_PORT));

      this.mongo = new Mongo(host, port);
      this.db = this.mongo.getDB(name);

      DBObject uid = new BasicDBObject("uid", 1);
      DBObject unique = new BasicDBObject("unique", true);

      this.db.getCollection(TBL_ELEMENTS).ensureIndex(uid, unique);
      this.db.getCollection(TBL_PROEPRTIES).ensureIndex("key");

      this.collections.put(Source.class.getName(), this.db.getCollection(TBL_SOURCES));
      this.collections.put(Element.class.getName(), this.db.getCollection(TBL_ELEMENTS));
      this.collections.put(Property.class.getName(), this.db.getCollection(TBL_PROEPRTIES));
      this.collections.put(ActionLog.class.getName(), this.db.getCollection(TBL_ACTIONLOGS));

      this.connected = true;

    } catch (NumberFormatException e) {

      LOG.error("Cannot parse port information! Error: {}", e.getMessage());
      throw new C3POPersistenceException("Could not parse port information", e);

    } catch (UnknownHostException e) {

      LOG.error("Could not find host! Error: {}", e.getMessage());
      throw new C3POPersistenceException("Could not find host", e);

    } catch (MongoException e) {

      LOG.error("The mongo driver threw an exception! Error: {}", e.getMessage());
      throw new C3POPersistenceException("A mongo specific error occurred", e);

    }

  }

  @Override
  public void close() throws C3POPersistenceException {
    if (this.isConnected() && this.mongo != null) {
      this.mongo.close();
      this.db = null;
      this.connected = false;
    }
  }

  @Override
  public boolean isConnected() {
    return this.connected;
  }

  @Override
  public Cache getCache() {
    return this.dbCache;
  }

  @Override
  public void setCache(Cache c) {
    this.dbCache = c;

  }

  /**
   * {@inheritDoc}
   * 
   * Clears the {@link DBCache} and removes all numeric statistics that are
   * persisted by this provider.
   */
  @Override
  public void clearCache() {
    synchronized (TBL_NUMERIC_STATISTICS) {

      this.dbCache.clear();
      this.db.getCollection(TBL_NUMERIC_STATISTICS).remove(new BasicDBObject());

    }
  }

  @Override
  public <T extends Model> Iterator<T> find(Class<T> clazz, Filter filter) {

    DBObject query = this.getCachedFilter(filter);

    DBCollection dbCollection = this.getCollection(clazz);
    ModelDeserializer modelDeserializer = this.getDeserializer(clazz);

    if (dbCollection == null) {
      LOG.warn("No collection found for clazz [{}]", clazz.getName());
      return new MongoIterator<T>(modelDeserializer, null);
    }

    DBCursor cursor = dbCollection.find(query);

    return new MongoIterator<T>(modelDeserializer, cursor);
  }

  @Override
  public <T extends Model> void insert(T object) {

    DBCollection dbCollection = this.getCollection(object.getClass());
    ModelSerializer serializer = this.getSerializer(object.getClass());

    dbCollection.insert(serializer.serialize(object));

  }

  @Override
  public <T extends Model> void update(T object) {
    // TODO update object
    DBCollection dbCollection = this.getCollection(object.getClass());
    // dbCollection.update(q, o, true, false);

  }

  @Override
  public <T extends Model> void remove(T object) {
    DBCollection dbCollection = this.getCollection(object.getClass());
    ModelSerializer serializer = this.getSerializer(object.getClass());

    dbCollection.remove(serializer.serialize(object));

  }

  @Override
  public <T extends Model> void remove(Class<T> clazz, Filter filter) {

    DBObject query = this.getCachedFilter(filter);
    DBCollection dbCollection = this.getCollection(clazz);
    dbCollection.findAndRemove(query);

  }

  @Override
  public <T extends Model> long count(Class<T> clazz, Filter filter) {

    DBObject query = this.getCachedFilter(filter);
    DBCollection dbCollection = this.getCollection(clazz);
    return dbCollection.count(query);

  }

  @Override
  public <T extends Model> List<String> distinct(Class<T> clazz, String f, Filter filter) {

    DBObject query = this.getCachedFilter(filter);
    DBCollection dbCollection = this.getCollection(clazz);

    return dbCollection.distinct(f, query);

  }

  @Override
  public <T extends Model> Map<String, Integer> getValueHistogramFor(Class<T> clazz, Property p, Filter filter)
      throws UnsupportedOperationException {

    return null;
  }

  @Override
  public NumericStatistics getNumericStatistics(Property p, Filter filter) throws UnsupportedOperationException,
      IllegalArgumentException {

    if (p == null) {
      throw new IllegalArgumentException("No property provider. Cannot aggregate");
    }

    if (!p.getType().equals(PropertyType.INTEGER.name()) && !p.getType().equals(PropertyType.FLOAT.name())) {
      throw new IllegalArgumentException("Cannot aggregate a non numeric property: " + p.getKey());
    }

    filter = (filter == null) ? new Filter() : filter;

    DBCollection statsCollection = this.db.getCollection(TBL_NUMERIC_STATISTICS);
    int key = getCachedResultId(p.getKey(), filter);
    DBCursor cursor = statsCollection.find(new BasicDBObject("_id", key));

    NumericStatistics result = null;
    if (cursor.count() == 0) {

      DBObject object = this.numericMapReduce(p.getKey(), filter);
      result = this.parseNumericStatistics(object);

    } else {

      DBObject next = (DBObject) cursor.next().get("value");
      result = this.parseNumericStatistics(next);

    }

    return result;
  }

  private DBObject numericMapReduce(String property, Filter filter) {
    int key = getCachedResultId(property, filter);

    DBCollection elmnts = getCollection(Element.class);
    DBObject query = this.getCachedFilter(filter);
    query.put("metadata." + property, new BasicDBObject("$exists", true));

    String map = AGGREGATE_MAP.replaceAll("@1", key + "").replaceAll("@2", property);
    MapReduceCommand cmd = new MapReduceCommand(elmnts, map, AGGREGATE_REDUCE, TBL_NUMERIC_STATISTICS, MERGE, query);
    cmd.setFinalize(AGGREGATE_FINALIZE);

    elmnts.mapReduce(cmd);

    DBCollection statsCollection = this.db.getCollection(TBL_NUMERIC_STATISTICS);
    DBCursor cursor = statsCollection.find(new BasicDBObject("_id", key));

    if (cursor.count() == 0) {
      return null;
    }

    return (DBObject) cursor.next().get("value");
  }

  private int getCachedResultId(String property, Filter filter) {
    return (property + filter.hashCode()).hashCode();
  }

  private NumericStatistics parseNumericStatistics(DBObject object) {
    NumericStatistics result = null;

    if (object == null) {
      result = new NumericStatistics();
      
    } else {

      BasicDBObject obj = (BasicDBObject) object;

      long count = obj.getLong("count");
      double sum = obj.getDouble("sum");
      double min = obj.getDouble("min");
      double max = obj.getDouble("max");
      double avg = obj.getDouble("avg");
      double std = obj.getDouble("stddev");
      double var = obj.getDouble("variance");

      result = new NumericStatistics(count, sum, min, max, avg, std, var);
    }

    return result;
  }

  private <T extends Model> ModelSerializer getSerializer(Class<T> clazz) {
    return this.serializers.get(clazz.getName());
  }

  private <T extends Model> ModelDeserializer getDeserializer(Class<T> clazz) {
    return this.deserializers.get(clazz.getName());
  }

  private <T extends Model> DBCollection getCollection(Class<T> clazz) {
    return this.collections.get(clazz.getName());
  }

  /**
   * Checks if the {@link DBCache} has a filter that equals the given filter. If
   * yes, then the object that is stored under the last filter query key within
   * the cache is returned. If the last filter is null, or does not equal, then
   * the cache is update and the correct filter is returned.
   * 
   * 
   * @param f
   *          the filter to check.
   * @return the cached filter or the updated version.
   * @see MongoFilterSerializer;
   */
  private DBObject getCachedFilter(Filter f) {
    Filter filter = (Filter) this.dbCache.getObject(LAST_FILTER);
    DBObject result = null;

    if (filter != null && filter.equals(f)) {
      result = (DBObject) this.dbCache.getObject(LAST_FILTER_QUERY);
    } else {
      result = this.filterSerializer.serialize(f);
      this.dbCache.put(LAST_FILTER, f);
      this.dbCache.put(LAST_FILTER_QUERY, result);
    }

    return result;

  }

  // -- TO BE REMOVED IN VERSION 0.4.0

  @Override
  public DB connect(Map<Object, Object> config) {
    return null;
  }

  @Override
  public DB getDB() {
    return this.db;
  }

  @Override
  public DBCursor findAll(String collection) {
    return this.db.getCollection(collection).find();
  }

  @Override
  public DBCursor find(String collection, DBObject ref) {
    return this.db.getCollection(collection).find(ref);
  }

  @Override
  public DBCursor find(String collection, DBObject ref, DBObject keys) {
    return this.db.getCollection(collection).find(ref, keys);
  }

  @Override
  public List distinct(String collection, String key) {
    return this.db.getCollection(collection).distinct(key);
  }

  @Override
  public List distinct(String collection, String key, DBObject query) {
    return this.db.getCollection(collection).distinct(key, query);
  }

  @Override
  public void insert(String collection, DBObject data) {
    this.db.getCollection(collection).insert(data);
  }

  @Override
  public long count(String collection) {
    return this.db.getCollection(collection).getCount();
  }

  @Override
  public long count(String collection, DBObject query) {
    return this.db.getCollection(collection).count(query);
  }

  @Override
  public MapReduceOutput mapreduce(String collection, MapReduceCommand cmd) {
    return this.db.getCollection(collection).mapReduce(cmd);
  }

}
