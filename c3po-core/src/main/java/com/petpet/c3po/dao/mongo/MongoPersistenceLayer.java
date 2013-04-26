package com.petpet.c3po.dao.mongo;

import static com.mongodb.MapReduceCommand.OutputType.INLINE;
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
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.dao.DBCache;
import com.petpet.c3po.utils.DataHelper;
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
   * An internally managed table for element property value histograms. This
   * table is managed by this concrete implementation and is just for
   * optimization purposes.
   */
  private static final String TBL_HISTOGRAMS = "histograms";

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
  public static final String AGGREGATE_MAP = "function map() {if (this.metadata['@2'] != null) {emit(@1,{sum: this.metadata['@2'].value, min: this.metadata['@2'].value,max:this.metadata['@2'].value,count:1,diff: 0,});}}";

  /**
   * The reduce of the aggregation functions.
   */
  public static final String AGGREGATE_REDUCE = "function reduce(key, values) {var a = values[0];for (var i=1; i < values.length; i++){var b = values[i];var delta = a.sum/a.count - b.sum/b.count;var weight = (a.count * b.count)/(a.count + b.count);a.diff += b.diff + delta*delta*weight;a.sum += b.sum;a.count += b.count;a.min = Math.min(a.min, b.min);a.max = Math.max(a.max, b.max);}return a;}";

  /**
   * A finalize function for the aggregation map reduce job, to calculate the
   * average, standard deviation and variance.
   */
  public static final String AGGREGATE_FINALIZE = "function finalize(key, value){ value.avg = value.sum / value.count;value.variance = value.diff / value.count;value.stddev = Math.sqrt(value.variance);return value;}";

  /**
   * A javascript Map function for building a histogram of a specific property.
   * All occurrences of that property are used (if they do not have conflcited
   * values). Note that there is a '@1' wildcard that has to be replaced with
   * the id of the desired property, prior to usage.
   */
  public static final String HISTOGRAM_MAP = "function map() {if (this.metadata['@1'] != null) {if (this.metadata['@1'].status !== 'CONFLICT') {emit(this.metadata['@1'].value, 1);}else{emit('Conflicted', 1);}} else {emit('Unknown', 1);}}";

  /**
   * A javascript Map function for building a histogram over a specific date
   * property. All occurrences of that property are used. If they are conflicted
   * then they are aggregated under one key 'Conflcited'. If the property is
   * missing, then the values are aggregated under the key 'Unknown'. Otherwise
   * the year is used as the key. Note that there is a '@1' wildcard that has to
   * be replaced with the id of the desired property, prior to usage.
   */
  public static final String DATE_HISTOGRAM_MAP = "function () {if (this.metadata['created'] != null && this.metadata['created'].value != undefined) {if (this.metadata['created'].status !== 'CONFLICT') {var date = new Date(this.metadata['created'].value);emit(date.getFullYear(), 1);}else{emit('Conflicted', 1);}}else{emit('Unknown', 1);}}";

  /**
   * A javascript Map function for building a histogram with fixed bin size. It
   * takes two wild cards as parameters - The @1 is the numeric property and the @2
   * is the bin size. The result contains the bins, where the id is from 0 to n
   * and the value is the number of occurrences. Note that each bin has a fixed
   * size so the label can be easily calculated. For example the id 0 marks the
   * number of elements where the numeric property was between 0 and the width,
   * the id 1 marks the number of elements where the numeric property was
   * between the width and 2*width and so on.
   */
  public static final String NUMERIC_HISTOGRAM_MAP = "function () {if (this.metadata['@1'] != null) {if (this.metadata['@1'].status !== 'CONFLICT') {var idx = Math.floor(this.metadata['@1'].value / @2);emit(idx, 1);} else {emit('Conflicted', 1);}}else{emit('Unknown', 1);}}";

  /**
   * The reduce function for the {@link Constants#HISTOGRAM_MAP}.
   */
  public static final String HISTOGRAM_REDUCE = "function reduce(key, values) {var res = 0;values.forEach(function (v) {res += v;});return res;}";

  private Mongo mongo;

  private DB db;

  private Cache dbCache;

  private boolean connected;

  private Map<String, MongoModelDeserializer> deserializers;

  private Map<String, MongoModelSerializer> serializers;

  private Map<String, DBCollection> collections;

  private MongoFilterSerializer filterSerializer;

  /**
   * The constructor initializes all needed objects, such as the serializers and
   * deserializers.
   */
  public MongoPersistenceLayer() {
    this.deserializers = new HashMap<String, MongoModelDeserializer>();
    this.deserializers.put(Element.class.getName(), new MongoElementDeserialzer(this));
    this.deserializers.put(Property.class.getName(), new MongoPropertyDeserialzer());
    this.deserializers.put(Source.class.getName(), new MongoSourceDeserializer());
    this.deserializers.put(ActionLog.class.getName(), new MongoActionLogDeserializer());

    this.serializers = new HashMap<String, MongoModelSerializer>();
    this.serializers.put(Element.class.getName(), new MongoElementSerializer());
    this.serializers.put(Property.class.getName(), new MongoPropertySerializer());
    this.serializers.put(Source.class.getName(), new MongoSourceSerializer());
    this.serializers.put(ActionLog.class.getName(), new MongoActionLogSerializer());

    this.filterSerializer = new MongoFilterSerializer();

    this.collections = new HashMap<String, DBCollection>();
    
  }

  /**
   * Establishes the connection to mongo database. This method relies on the
   * following configs being passed as arguments: <br>
   * db.name <br>
   * db.host <br>
   * db.port <br>
   * 
   * Once the connection is open, the method will ensure that the mongo
   * collections and indexes are created.
   * 
   * @throws C3POPersistenceException
   *           if something goes wrong. Make sure to check the cause of the
   *           exception.
   */
  @Override
  public void establishConnection(Map<String, String> config) throws C3POPersistenceException {
    this.close();

    if (config == null || config.keySet().isEmpty()) {
      throw new C3POPersistenceException("Cannot establish connection. No configuration provided");
    }

    try {
      String name = config.get(CNF_DB_NAME);
      String host = config.get(CNF_DB_HOST);
      int port = Integer.parseInt(config.get(CNF_DB_PORT));

      this.mongo = new Mongo(host, port);
      this.db = this.mongo.getDB(name);

      DBObject uid = new BasicDBObject("uid", 1);
      DBObject key = new BasicDBObject("key", 1);
      DBObject unique = new BasicDBObject("unique", true);

      this.db.getCollection(TBL_ELEMENTS).ensureIndex(uid, unique);
      this.db.getCollection(TBL_PROEPRTIES).ensureIndex(key, unique);

      this.collections.put(Source.class.getName(), this.db.getCollection(TBL_SOURCES));
      this.collections.put(Element.class.getName(), this.db.getCollection(TBL_ELEMENTS));
      this.collections.put(Property.class.getName(), this.db.getCollection(TBL_PROEPRTIES));
      this.collections.put(ActionLog.class.getName(), this.db.getCollection(TBL_ACTIONLOGS));
      
      if (this.dbCache == null) {
        DBCache cache = new DBCache();
        cache.setPersistence(this);
        this.dbCache = cache;
      }

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

  /**
   * If the connection is open, then this method closes it. Otherwise, nothing
   * happens.
   */
  @Override
  public void close() throws C3POPersistenceException {
    if (this.isConnected() && this.mongo != null) {
      this.mongo.close();
      this.db = null;
      this.connected = false;
    }
  }

  /**
   * Whether or not the persistence layer is connected.
   */
  @Override
  public boolean isConnected() {
    return this.connected;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Cache getCache() {
    return this.dbCache;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCache(Cache c) {
    this.dbCache = c;
  }

  /**
   * Clears the {@link DBCache} and removes all internally managed mongo
   * collection that store cached results.
   */
  @Override
  public void clearCache() {
    synchronized (TBL_NUMERIC_STATISTICS) {

      this.dbCache.clear();

      BasicDBObject all = new BasicDBObject();
      this.db.getCollection(TBL_NUMERIC_STATISTICS).remove(all);
      this.db.getCollection(TBL_HISTOGRAMS).remove(all);

    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> Iterator<T> find(Class<T> clazz, Filter filter) {

    DBObject query = this.getCachedFilter(filter);

    DBCollection dbCollection = this.getCollection(clazz);
    MongoModelDeserializer modelDeserializer = this.getDeserializer(clazz);

    if (dbCollection == null) {
      LOG.warn("No collection found for clazz [{}]", clazz.getName());
      return new MongoIterator<T>(modelDeserializer, null);
    }

    DBCursor cursor = dbCollection.find(query);

    return new MongoIterator<T>(modelDeserializer, cursor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> void insert(T object) {

    DBCollection dbCollection = this.getCollection(object.getClass());
    MongoModelSerializer serializer = this.getSerializer(object.getClass());

    dbCollection.insert(serializer.serialize(object));

  }

  /**
   * Inserts or updates all objects that correspond to the given filter. Note,
   * however, that if the object or the passed filter is null, nothing will
   * happen.
   * 
   * @param object
   *          the object to update.
   * @param filter
   *          the filter to apply in order to select the objects that will be
   *          updated.
   */
  @Override
  public <T extends Model> void update(T object, Filter f) {

    DBObject filter = this.getCachedFilter(f);

    if (filter.keySet().isEmpty()) {
      LOG.warn("Cannot update an object without a filter");
      return;
    }

    if (object == null) {
      LOG.warn("Cannot update a null object");
      return;
    }

    DBCollection dbCollection = this.getCollection(object.getClass());
    MongoModelSerializer serializer = this.getSerializer(object.getClass());
    DBObject objectToUpdate = serializer.serialize(object);
    dbCollection.update(filter, objectToUpdate, true, true);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> void remove(T object) {
    DBCollection dbCollection = this.getCollection(object.getClass());
    MongoModelSerializer serializer = this.getSerializer(object.getClass());

    dbCollection.remove(serializer.serialize(object));

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> void remove(Class<T> clazz, Filter filter) {

    DBObject query = this.getCachedFilter(filter);
    DBCollection dbCollection = this.getCollection(clazz);
    dbCollection.remove(query);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> long count(Class<T> clazz, Filter filter) {

    DBObject query = this.getCachedFilter(filter);
    DBCollection dbCollection = this.getCollection(clazz);
    return dbCollection.count(query);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Model> List<String> distinct(Class<T> clazz, String f, Filter filter) {

    DBObject query = this.getCachedFilter(filter);
    DBCollection dbCollection = this.getCollection(clazz);

    return dbCollection.distinct(f, query);

  }

  /**
   * Gets a value histogram for the given property and the given filter. This
   * method works only over the elements mongo collection. Note that it does
   * some internal caching and relies upon the fact that the
   * {@link PersistenceLayer#clearCache()} method will be called every time new
   * elements are added.
   */
  @Override
  public <T extends Model> Map<String, Long> getValueHistogramFor(Property p, Filter filter)
      throws UnsupportedOperationException {

    Map<String, Long> histogram = new HashMap<String, Long>();

    filter = (filter == null) ? new Filter() : filter;

    DBCollection histCollection = this.db.getCollection(TBL_HISTOGRAMS);
    int key = getCachedResultId(p.getKey(), filter);
    System.out.println(key);
    DBCursor cursor = histCollection.find(new BasicDBObject("_id", key));

    if (cursor.count() == 0) {
      // no cached results for this histogram
      DBObject object = this.histogramMapReduce(key, p, filter);
      histogram = this.parseHistogramResults(object);

    } else {
      // process
      DBObject object = (DBObject) cursor.next().get("results");
      histogram = this.parseHistogramResults(object);
    }

    return histogram;
  }

  /**
   * Gets the numeric statistics for the given property and the given filter.
   * This method works only over the elements mongo collection. Note that it
   * does some internal caching and relies upon the fact that the
   * {@link PersistenceLayer#clearCache()} method will be called every time new
   * elements are added. Also note that the property has to be numeric and that
   * it cannot be null.
   * 
   * @throws IllegalArgumentException
   *           if the property is null or not numeric.
   */
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

  /**
   * This method obtains the histogram for the given property and filter by
   * executing a map reduce job. It stores the results to an internally managed
   * table under the given key.
   * 
   * @param key
   *          the key under which to store the results within the cached table.
   * @param p
   *          the property that will be map reduced
   * @param filter
   *          the filter to apply
   * @return a {@link DBObject} containing the results.
   */
  private DBObject histogramMapReduce(int key, Property p, Filter filter) {
    String map = "";

    DBObject query = this.getCachedFilter(filter);

    if (p.getType().equals(PropertyType.DATE.toString())) {

      map = DATE_HISTOGRAM_MAP.replace("@1", p.getKey());

    } else if (p.getType().equals(PropertyType.INTEGER.toString()) || p.getType().equals(PropertyType.FLOAT.toString())) {

      // TODO allow possibility for options in order to specify the bin width
      // and potentially other options.
      // hard coded bin width: 10
      map = NUMERIC_HISTOGRAM_MAP.replace("@1", p.getKey()).replace("@2", "10");

    } else {

      map = HISTOGRAM_MAP.replace("@1", p.getId());
    }

    LOG.debug("Executing histogram map reduce job with following map:\n{}", map);
    LOG.debug("filter query is:\n{}", query);
    DBCollection elmnts = getCollection(Element.class);
    MapReduceCommand cmd = new MapReduceCommand(elmnts, map, HISTOGRAM_REDUCE, null, INLINE, query);

    MapReduceOutput output = elmnts.mapReduce(cmd);
    List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");

    DBCollection histCollection = this.db.getCollection(TBL_HISTOGRAMS);
    BasicDBObject old = new BasicDBObject("_id", key);
    BasicDBObject res = new BasicDBObject(old.toMap());
    res.put("results", results);
    histCollection.update(old, res, true, false);

    DBCursor cursor = histCollection.find(new BasicDBObject("_id", key));

    if (cursor.count() == 0) {
      return null;
    }

    return (DBObject) cursor.next().get("results");
  }

  /**
   * Executes a map reduce job in order to calculate the statistics for the
   * given property.
   * 
   * @param property
   *          the property (has to be numeric)
   * @param filter
   *          the filter to apply
   * @return a {@link DBObject} with the results.
   */
  private DBObject numericMapReduce(String property, Filter filter) {
    int key = getCachedResultId(property, filter);

    DBCollection elmnts = getCollection(Element.class);
    DBObject query = this.getCachedFilter(filter);

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

  /**
   * Generates a key out of the property and filter that is used to uniquely
   * identify cached results.
   * 
   * @param property
   *          the property of for which an operation was executed.
   * @param filter
   *          the filter that was used.
   * @return the generated key.
   */
  private int getCachedResultId(String property, Filter filter) {
    return (property + filter.hashCode()).hashCode();
  }

  /**
   * Parses the histogram results out of a {@link DBObject}. This method assumes
   * that the passed db object contains a list of {@link DBObject}s with every
   * result. (As it is outputted by the map reduce job).
   * 
   * @param object
   *          the object to parse.
   * @return the histogram map.
   */
  private Map<String, Long> parseHistogramResults(DBObject object) {
    Map<String, Long> histogram = new HashMap<String, Long>();

    if (object == null) {
      return histogram;
    }

    List<BasicDBObject> results = (List<BasicDBObject>) object;
    for (final BasicDBObject dbo : results) {
      histogram.put(DataHelper.removeTrailingZero(dbo.getString("_id")), dbo.getLong("value"));
    }

    return histogram;
  }

  /**
   * Parses the numeric statistics out of given {@link DBObject}.
   * 
   * @param object
   *          the object to parse.
   * @return a {@link NumericStatistics} object that wraps the results.
   */
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

  /**
   * Gets the correct serializer for that class.
   * 
   * @param clazz
   *          the class that we want to serialize.
   * @return the serializer.
   */
  private <T extends Model> MongoModelSerializer getSerializer(Class<T> clazz) {
    return this.serializers.get(clazz.getName());
  }

  /**
   * Gets the correct deserializer for the given class.
   * 
   * @param clazz
   *          the class that we want to deserialize.
   * @return the deserializer.
   */
  private <T extends Model> MongoModelDeserializer getDeserializer(Class<T> clazz) {
    return this.deserializers.get(clazz.getName());
  }

  /**
   * Gets the correct mongo {@link DBCollection} for the given class.
   * 
   * @param clazz
   *          the class we want to store.
   * @return the {@link DBCollection}.
   */
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
