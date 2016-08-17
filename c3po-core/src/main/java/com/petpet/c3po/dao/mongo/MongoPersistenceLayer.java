/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.petpet.c3po.dao.mongo;

import static com.mongodb.MapReduceCommand.OutputType.INLINE;
import static com.mongodb.MapReduceCommand.OutputType.MERGE;

import java.net.UnknownHostException;
import java.util.*;

import com.mongodb.*;
import com.petpet.c3po.utils.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * A MongoDB (http://www.mongodb.org) specific back-end persistence layer
 * implementation.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 *
 */
public class MongoPersistenceLayer implements PersistenceLayer {

    /**
     * A default logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger( MongoPersistenceLayer.class );

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
    public static final String AGGREGATE_MAP = "function map() {if (this['@2'] != null) {emit(@1,{sum: this['@2'].value, min: this['@2'].value,max: this['@2'].value,count:1,diff: 0,});}}";

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
    public static final String HISTOGRAM_MAP = "function map() {if (this['@1'] != null) {if (this['@1'].status !== 'CONFLICT') {emit(this['@1'].value, 1);}else{emit('CONFLICT', 1);}} else {emit('Unknown', 1);}}";

    /**
     * A javascript Map function for building a histogram over a specific date
     * property. All occurrences of that property are used. If they are conflicted
     * then they are aggregated under one key 'Conflcited'. If the property is
     * missing, then the values are aggregated under the key 'Unknown'. Otherwise
     * the year is used as the key. Note that there is a '@1' wildcard that has to
     * be replaced with the id of the desired property, prior to usage.
     */
    public static final String DATE_HISTOGRAM_MAP = "function () {if (this['created'] != null && this['created'].value != undefined) {if (this['created'].status !== 'CONFLICT') {var date = new Date(this['created'].value);emit(date.getFullYear(), 1);}else{emit('CONFLICT', 1);}}else{emit('Unknown', 1);}}";

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
    public static final String NUMERIC_HISTOGRAM_MAP = "function () {if (this['@1'] != null) {if (this['@1'].status !== 'CONFLICT') {var idx = Math.floor(this['@1'].value / @2);emit(idx, 1);} else {emit('CONFLICT', 1);}}else{emit('Unknown', 1);}}";

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

    private void setResult(WriteResult result) {
        this.writeResult = result;
    }

    public Map<String, Object> getResult() {
        Map<String, Object> result=new HashMap<String, Object>();
        if (writeResult.getField("nInserted")!=null)
            result.put("nInserted",writeResult.getField("nInserted"));
        if (writeResult.getField("nMatched")!=null)
            result.put("nMatched",writeResult.getField("nMatched"));
        if (writeResult.getField("nModified")!=null)
            result.put("nModified",writeResult.getField("nModified"));
        if (writeResult.getField("nUpserted")!=null)
            result.put("nUpserted",writeResult.getField("nUpserted"));
        if (writeResult.getField("nRemoved")!=null)
            result.put("nRemoved",writeResult.getField("nRemoved"));
        result.put("count",writeResult.getN());
        return result;
    }

    private WriteResult writeResult;

    /**
     * The constructor initializes all needed objects, such as the serializers and
     * deserializers.
     */
    public MongoPersistenceLayer() {
        this.deserializers = new HashMap<String, MongoModelDeserializer>();
        this.deserializers.put( Element.class.getName(), new MongoElementDeserialzer( this ) );
        this.deserializers.put( Property.class.getName(), new MongoPropertyDeserialzer() );
        this.deserializers.put( Source.class.getName(), new MongoSourceDeserializer() );
        this.deserializers.put( ActionLog.class.getName(), new MongoActionLogDeserializer() );

        this.serializers = new HashMap<String, MongoModelSerializer>();
        this.serializers.put( Element.class.getName(), new MongoElementSerializer() );
        this.serializers.put( Property.class.getName(), new MongoPropertySerializer() );
        this.serializers.put( Source.class.getName(), new MongoSourceSerializer() );
        this.serializers.put( ActionLog.class.getName(), new MongoActionLogSerializer() );

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
    public void establishConnection( Map<String, String> config ) throws C3POPersistenceException {
        this.close();

        if ( config == null || config.keySet().isEmpty() ) {
            throw new C3POPersistenceException( "Cannot establish connection. No configuration provided" );
        }

        try {
            String name = config.get( CNF_DB_NAME );
            String host = config.get( CNF_DB_HOST );
            int port = Integer.parseInt( config.get( CNF_DB_PORT ) );

            this.mongo = new Mongo( host, port );
            this.db = this.mongo.getDB( name );

            DBObject uid = new BasicDBObject( "uid", 1 );
            DBObject key = new BasicDBObject( "key", 1 );
            DBObject unique = new BasicDBObject( "unique", true );

            this.db.getCollection( TBL_ELEMENTS ).ensureIndex( uid, unique );
            this.db.getCollection( TBL_PROEPRTIES ).ensureIndex( key, unique );

            this.collections.put( Source.class.getName(), this.db.getCollection( TBL_SOURCES ) );
            this.collections.put( Element.class.getName(), this.db.getCollection( TBL_ELEMENTS ) );
            this.collections.put( Property.class.getName(), this.db.getCollection( TBL_PROEPRTIES ) );
            this.collections.put( ActionLog.class.getName(), this.db.getCollection( TBL_ACTIONLOGS ) );

            if ( this.dbCache == null ) {
                DBCache cache = new DBCache();
                cache.setPersistence( this );
                this.dbCache = cache;
            }

            this.connected = true;

        } catch ( NumberFormatException e ) {

            LOG.error( "Cannot parse port information! Error: {}", e.getMessage() );
            throw new C3POPersistenceException( "Could not parse port information", e );

        } catch ( UnknownHostException e ) {

            LOG.error( "Could not find host! Error: {}", e.getMessage() );
            throw new C3POPersistenceException( "Could not find host", e );

        } catch ( MongoException e ) {

            LOG.error( "The mongo driver threw an exception! Error: {}", e.getMessage() );
            throw new C3POPersistenceException( "A mongo specific error occurred", e );

        }

    }

    /**
     * If the connection is open, then this method closes it. Otherwise, nothing
     * happens.
     */
    @Override
    public void close() throws C3POPersistenceException {
        if ( this.isConnected() && this.mongo != null ) {
            this.db.cleanCursors( true );
            this.mongo.close();
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
    public void setCache( Cache c ) {
        this.dbCache = c;
    }

    /**
     * Clears the {@link DBCache} and removes all internally managed mongo
     * collection that store cached results.
     */
    @Override
    public void clearCache() {
        synchronized ( TBL_NUMERIC_STATISTICS ) {

            this.dbCache.clear();

            BasicDBObject all = new BasicDBObject();
            this.db.getCollection( TBL_NUMERIC_STATISTICS ).remove( all );
            this.db.getCollection( TBL_HISTOGRAMS ).remove( all );

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Model> Iterator<T> find( Class<T> clazz, Filter filter ) {

        DBObject query = this.getCachedFilter( filter );

        DBCollection dbCollection = this.getCollection( clazz );
        MongoModelDeserializer modelDeserializer = this.getDeserializer( clazz );

        if ( dbCollection == null ) {
            LOG.warn( "No collection found for clazz [{}]", clazz.getName() );
            return new MongoIterator<T>( modelDeserializer, null );
        }

        DBCursor cursor = dbCollection.find( query );

        return new MongoIterator<T>( modelDeserializer, cursor );
    }


    public <T extends Model> DBCursor findRaw( Class<T> clazz, Filter filter ) {

        DBObject query = this.getCachedFilter( filter );

        DBCollection dbCollection = this.getCollection( clazz );

        if ( dbCollection == null ) {
            LOG.warn( "No collection found for clazz [{}]", clazz.getName() );
            return null;
        }

        DBCursor cursor = dbCollection.find( query );
        return cursor;
    }

    public <T extends Model> Iterator<T> findQ( Class<T> clazz, DBObject query ) {

        DBCollection dbCollection = this.getCollection( clazz );
        MongoModelDeserializer modelDeserializer = this.getDeserializer( clazz );

        if ( dbCollection == null ) {
            LOG.warn( "No collection found for clazz [{}]", clazz.getName() );
            return new MongoIterator<T>( modelDeserializer, null );
        }

        DBCursor cursor = dbCollection.find( query );

        return new MongoIterator<T>( modelDeserializer, cursor );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Model> void insert( T object ) {

        DBCollection dbCollection = this.getCollection( object.getClass() );
        MongoModelSerializer serializer = this.getSerializer( object.getClass() );

        WriteResult insert = dbCollection.insert(serializer.serialize(object));
        setResult(insert);

    }

    /**
     * Inserts or updates all objects that correspond to the given filter. Note,
     * however, that if the object or the passed filter is null, nothing will
     * happen. Note, also that the updated document is not replaced but $set is
     * used on the changed fields. This implies that the caller has to make sure,
     * the passed object has only the fields that will be updated. All other
     * fields should be null or empty.
     *
     * @param object
     *          the object to update.
     * @param filter
     *          the filter to apply in order to select the objects that will be
     *          updated.
     */
    @Override
    public <T extends Model> void update( T object, Filter f ) {
        DBObject filter = this.getCachedFilter( f );
        String filterString=filter.toString();
        if ( filter.keySet().isEmpty() ) {
            LOG.warn( "Cannot update an object without a filter" );
            return;
        }

        if ( object == null ) {
            LOG.warn( "Cannot update a null object" );
            return;
        }

        DBCollection dbCollection = this.getCollection( object.getClass() );
        MongoModelSerializer serializer = this.getSerializer( object.getClass() );
        DBObject objectToUpdate = serializer.serialize( object );
        BasicDBObject set = new BasicDBObject( "$set", objectToUpdate );
        String setString = set.toString();
        WriteResult update = dbCollection.update(filter, set, false, true);
        setResult(update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Model> void remove( T object ) {
        if ( object == null ) {
            return;
        }

        DBCollection dbCollection = this.getCollection( object.getClass() );
        MongoModelSerializer serializer = this.getSerializer( object.getClass() );

        WriteResult remove = dbCollection.remove(serializer.serialize(object));
        setResult(remove);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Model> void remove( Class<T> clazz, Filter filter ) {

        DBObject query = this.getCachedFilter( filter );
        DBCollection dbCollection = this.getCollection( clazz );
        WriteResult remove = dbCollection.remove(query);
        clearCache();
        setResult(remove);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Model> long count( Class<T> clazz, Filter filter ) {

        DBObject query = this.getCachedFilter( filter );
        DBCollection dbCollection = this.getCollection( clazz );
        return dbCollection.count( query );

    }


    public <T extends Model> long count( Class<T> clazz, DBObject query ) {

        DBCollection dbCollection = this.getCollection( clazz );
        return dbCollection.count( query );

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Model> List<String> distinct( Class<T> clazz, String f, Filter filter ) {

        DBObject query = this.getCachedFilter( filter );
        DBCollection dbCollection = this.getCollection( clazz );
        f = this.filterSerializer.mapFieldToProperty( f, new Object() );

        return dbCollection.distinct( f, query );

    }

    /**
     * Gets a value histogram for the given property and the given filter. This
     * method works only over the elements mongo collection. Note that it does
     * some internal caching and relies upon the fact that the
     * {@link PersistenceLayer#clearCache()} method will be called every time new
     * elements are added.
     */
    @Override
    public <T extends Model> Map<String, Long> getValueHistogramFor( Property p, Filter filter )
            throws UnsupportedOperationException {

        Map<String, Long> histogram = new HashMap<String, Long>();

        filter = (filter == null) ? new Filter() : filter;

        DBCollection histCollection = this.db.getCollection( TBL_HISTOGRAMS );
        int key = getCachedResultId( p.getKey(), filter );
        // System.out.println(key);
        DBCursor cursor = histCollection.find( new BasicDBObject( "_id", key ) );

        if ( cursor.count() == 0 ) {
            // no cached results for this histogram
            DBObject object = this.histogramMapReduce( key, p, filter );
            histogram = this.parseHistogramResults( object );

        } else {
            // process
            DBObject object =  this.histogramMapReduce( key, p, filter );//(DBObject) cursor.next().get( "results" );
            histogram = this.parseHistogramResults( object );
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
    public NumericStatistics getNumericStatistics( Property p, Filter filter ) throws UnsupportedOperationException,
            IllegalArgumentException {

        if ( p == null ) {
            throw new IllegalArgumentException( "No property provider. Cannot aggregate" );
        }

        if ( !p.getType().equals( PropertyType.INTEGER.name() ) && !p.getType().equals( PropertyType.FLOAT.name() ) ) {
            throw new IllegalArgumentException( "Cannot aggregate a non numeric property: " + p.getKey() );
        }

        filter = (filter == null) ? new Filter() : filter;

        DBCollection statsCollection = this.db.getCollection( TBL_NUMERIC_STATISTICS );
        int key = getCachedResultId( p.getKey(), filter );
        DBCursor cursor = statsCollection.find( new BasicDBObject( "_id", key ) );

        NumericStatistics result = null;
        if ( cursor.count() == 0 ) {

            DBObject object = this.numericMapReduce( p.getKey(), filter );
            result = this.parseNumericStatistics( object );

        } else {

            DBObject next = this.numericMapReduce( p.getKey(), filter );//(DBObject) cursor.next().get( "value" );
            result = this.parseNumericStatistics( next );

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
    private DBObject histogramMapReduce( int key, Property p, Filter filter ) {
        long start = System.currentTimeMillis();
        String map = "";

        DBObject query = this.getCachedFilter( filter );

        if ( p.getType().equals( PropertyType.DATE.toString() ) ) {

            map = DATE_HISTOGRAM_MAP.replace( "@1", p.getKey() );

        } else if ( p.getType().equals( PropertyType.INTEGER.toString() )
                || p.getType().equals( PropertyType.FLOAT.toString() ) ) {

            // TODO allow possibility for options in order to specify the bin width
            // and potentially other options.
            // hard coded bin width: 10
            map = NUMERIC_HISTOGRAM_MAP.replace( "@1", p.getKey() ).replace( "@2", "10" );

        } else {

            map = HISTOGRAM_MAP.replace( "@1", p.getId() );
        }

        LOG.debug( "Executing histogram map reduce job with following map:\n{}", map );
        LOG.debug( "filter query is:\n{}", query );
        DBCollection elmnts = getCollection( Element.class );
        MapReduceCommand cmd = new MapReduceCommand( elmnts, map, HISTOGRAM_REDUCE, null, INLINE, query );

        MapReduceOutput output = elmnts.mapReduce( cmd );
        List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get( "results" );

        DBCollection histCollection = this.db.getCollection( TBL_HISTOGRAMS );
        BasicDBObject old = new BasicDBObject( "_id", key );
        BasicDBObject res = new BasicDBObject( old.toMap() );
        res.put( "results", results );
        histCollection.update( old, res, true, false );

        DBCursor cursor = histCollection.find( new BasicDBObject( "_id", key ) );

        if ( cursor.count() == 0 ) {
            return null;
        }
        long end = System.currentTimeMillis();
        LOG.debug( "The map-reduce job took {} seconds", (end - start)/1000 );
        return (DBObject) cursor.next().get( "results" );
    }



    public List<BasicDBObject> mapReduceRaw(String map, String reduce, Filter filter ){
        long start = System.currentTimeMillis();
        DBObject query = this.getCachedFilter( filter );
        DBCollection elmnts = getCollection( Element.class );
        MapReduceCommand cmd = new MapReduceCommand( elmnts, map, reduce, null, INLINE, query );

        MapReduceOutput output = elmnts.mapReduce( cmd );
        List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get( "results" );
        long end = System.currentTimeMillis();
        LOG.debug( "The map-reduce job took {} seconds", (end - start)/1000 );
        return results;

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
    private DBObject numericMapReduce( String property, Filter filter ) {
        long start = System.currentTimeMillis();
        int key = getCachedResultId( property, filter );

        DBCollection elmnts = getCollection( Element.class );
        DBObject query = this.getCachedFilter( filter );

        String map = AGGREGATE_MAP.replaceAll( "@1", key + "" ).replaceAll( "@2", property );
        MapReduceCommand cmd = new MapReduceCommand( elmnts, map, AGGREGATE_REDUCE, TBL_NUMERIC_STATISTICS, MERGE, query );
        cmd.setFinalize( AGGREGATE_FINALIZE );

        elmnts.mapReduce( cmd );

        DBCollection statsCollection = this.db.getCollection( TBL_NUMERIC_STATISTICS );
        DBCursor cursor = statsCollection.find( new BasicDBObject( "_id", key ) );

        if ( cursor.count() == 0 ) {
            return null;
        }
        long end = System.currentTimeMillis();
        LOG.debug( "The map-reduce job took {} seconds", (end - start)/1000 );
        return (DBObject) cursor.next().get( "value" );
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
    private int getCachedResultId( String property, Filter filter ) {
        return (property + filter.hashCode()).hashCode();
    }

    private int getCachedResultId( List<String> properties, Filter filter ) {
        return (properties.hashCode() + filter.hashCode());
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
    private Map<String, Long> parseHistogramResults( DBObject object ) {
        Map<String, Long> histogram = new HashMap<String, Long>();

        if ( object == null ) {
            return histogram;
        }

        List<BasicDBObject> results = (List<BasicDBObject>) object;
        for ( final BasicDBObject dbo : results ) {
            histogram.put( DataHelper.removeTrailingZero( dbo.getString( "_id" ) ), dbo.getLong( "value" ) );
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
    private NumericStatistics parseNumericStatistics( DBObject object ) {
        NumericStatistics result = null;


        if (object == null) {
            result = new NumericStatistics();

        } else {

            BasicDBObject obj = (BasicDBObject) object;

            long count = obj.getLong("count");
            double sum, min, max, avg, std, var;
            try {
                sum = obj.getDouble("sum");
            } catch (Exception e) {
                sum = 0;
            }
            try {
                min = obj.getDouble("min");
            } catch (Exception e) {
                min = 0;
            }
            try {
                max = obj.getDouble("max");
            } catch (Exception e) {
                max = 0;
            }
            try {
                avg = obj.getDouble("avg");
            } catch (Exception e) {
                avg = 0;
            }
            try {
                std = obj.getDouble("stddev");
            } catch (Exception e) {
                std = 0;
            }
            try {
                var = obj.getDouble("variance");
            } catch (Exception e) {
                var = 0;
            }
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
    private <T extends Model> MongoModelSerializer getSerializer( Class<T> clazz ) {
        return this.serializers.get( clazz.getName() );
    }

    /**
     * Gets the correct deserializer for the given class.
     *
     * @param clazz
     *          the class that we want to deserialize.
     * @return the deserializer.
     */
    private <T extends Model> MongoModelDeserializer getDeserializer( Class<T> clazz ) {
        return this.deserializers.get( clazz.getName() );
    }

    /**
     * Gets the correct mongo {@link DBCollection} for the given class.
     *
     * @param clazz
     *          the class we want to store.
     * @return the {@link DBCollection}.
     */
    public <T extends Model> DBCollection getCollection( Class<T> clazz ) {
        return this.collections.get( clazz.getName() );
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
    public DBObject getCachedFilter( Filter f ) {
        Filter filter = (Filter) this.dbCache.getObject( LAST_FILTER );
        DBObject result = null;

        //if ( filter != null && filter.equals( f ) ) {
        // result = (DBObject) this.dbCache.getObject( LAST_FILTER_QUERY );
        //} else {
        result = this.filterSerializer.serialize( f );
        //  this.dbCache.put( LAST_FILTER, f );
        //  this.dbCache.put( LAST_FILTER_QUERY, result );
        //}

        return result;

    }




    public <T extends Model> Map<String, Long> getValueHistogramFor( List<String> properties, Filter filter )
            throws UnsupportedOperationException {

        Map<String, Long> histogram = new HashMap<String, Long>();

        filter = (filter == null) ? new Filter() : filter;

        DBCollection histCollection = this.db.getCollection( TBL_HISTOGRAMS );
        int key = getCachedResultId( properties, filter );
        // System.out.println(key);
        DBCursor cursor = histCollection.find( new BasicDBObject( "_id", key ) );

        if ( cursor.count() == 0 ) {
            // no cached results for this histogram
            DBObject object = this.histogramMapReduce( key, properties, filter );
            histogram = this.parseHistogramResults( object );

        } else {
            // process
            DBObject object =  this.histogramMapReduce( key, properties, filter );//(DBObject) cursor.next().get( "results" );
            histogram = this.parseHistogramResults( object );
        }

        return histogram;
    }

    public Map<String, Map<String, Long>> parseMapReduce(DBObject object, List<String> properties){

        List<String> propertyStats=new ArrayList<String>();
        List<String> propertiesDate=new ArrayList<String>();
        List<String> propertiesNumbers=new ArrayList<String>();
        List<String> propertiesOthers=new ArrayList<String>();
        for (String property : properties ){
            Property p = this.getCache().getProperty(property);
            if (property.equals("size")){
                propertyStats.add(property);
            }
            else if ( p.getType().equals( PropertyType.DATE.toString() ) ) {
                propertiesDate.add(property);
            } else if ( p.getType().equals( PropertyType.INTEGER.toString() )
                    || p.getType().equals( PropertyType.FLOAT.toString() ) ) {
                propertiesNumbers.add(property);
            } else {
                propertiesOthers.add(property);
            }
        }

        Map<String, Map<String, Long>> histograms= new HashMap<String, Map<String, Long>>();
        Map<String, Long> histogram;

        if ( object == null ) {
         //   return histogram;
        }

        List<BasicDBObject> results = (List<BasicDBObject>) object;
        for ( final BasicDBObject dbo : results ) {

            String property =  ((BasicDBObject) dbo.get("_id")).getString("property");
            String value = ((BasicDBObject) dbo.get("_id")).getString("value");
            if (!property.equals("size")) {
                long count = dbo.getLong("value");
                if (histograms.get(property) == null) {
                    histogram = new HashMap<String, Long>();
                    histograms.put(property, histogram);
                } else {
                    histogram = histograms.get(property);
                }
                histogram.put(value, count);
            } else {
                if (histograms.get(property) == null) {
                    histogram = new HashMap<String, Long>();
                    histograms.put(property, histogram);
                } else {
                    histogram = histograms.get(property);
                }
                BasicDBObject v = (BasicDBObject) dbo.get("value");
                long sum, min, max, avg, std, var, count;
                try {
                    count = v.getLong("count");
                } catch (Exception e) {
                    count = 0;
                }
                try {
                    sum = v.getLong("sum");
                } catch (Exception e) {
                    sum = 0;
                }
                try {
                    min = v.getLong("min");
                } catch (Exception e) {
                    min = 0;
                }
                try {
                    max = v.getLong("max");
                } catch (Exception e) {
                    max = 0;
                }
                try {
                    avg = v.getLong("avg");
                } catch (Exception e) {
                    avg = 0;
                }
                try {
                    std = v.getLong("stddev");
                } catch (Exception e) {
                    std = 0;
                }
                try {
                    var = v.getLong("variance");
                } catch (Exception e) {
                    var = 0;
                }
                histogram.put("sum", sum);
                histogram.put("min", min);
                histogram.put("max", max);
                histogram.put("avg", avg);
                histogram.put("std", std);
                histogram.put("var", var);
                histogram.put("count", count);
            }


        }



        return histograms;
    }
    @Override
    public <T extends Model> Map<String, Map<String, Long>> getHistograms(List<String> properties, Filter filter){
        DBObject dbObject = mapReduce(0, properties, filter);
        Map<String, Map<String, Long>> histograms = parseMapReduce(dbObject, properties);
        return histograms;
    }


    public DBObject mapReduce(int key, List<String> properties, Filter filter){
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        List<String> propertyStats=new ArrayList<String>();
        List<String> propertiesDate=new ArrayList<String>();
        List<String> propertiesNumbers=new ArrayList<String>();
        List<String> propertiesOthers=new ArrayList<String>();
        for (String property : properties ){
            Property p = this.getCache().getProperty(property);
            if (property.equals("size")){
                propertyStats.add(property);
            }
            else if ( p.getType().equals( PropertyType.DATE.toString() ) ) {
                propertiesDate.add(property);
            } else if ( p.getType().equals( PropertyType.INTEGER.toString() )
                    || p.getType().equals( PropertyType.FLOAT.toString() ) ) {
                propertiesNumbers.add(property);
            } else {
                propertiesOthers.add(property);
            }
        }
        long start = System.currentTimeMillis();
        String map = "function() {\n" +
                "    var propertyStats = @1;\n" +
                "    var propertiesDate = @2;\n" +
                "    var propertiesNumbers = @3;\n" +
                "    var propertiesOthers = @4;\n" +
                "    for (x in propertiesOthers) {\n" +
                "        property = propertiesOthers[x];\n" +
                "        if (this[property] != null) {\n" +
                "            if (this[property].status != 'CONFLICT') {\n" +
                "                emit({\n" +
                "                    property: property,\n" +
                "                    value: this[property].values[0]\n" +
                "                }, 1);\n" +
                "            } else {\n" +
                "                emit({\n" +
                "                    property: property,\n" +
                "                    value: 'CONFLICT'\n" +
                "                }, 1);\n" +
                "            }\n" +
                "        } else {\n" +
                "            emit({\n" +
                "                property: property,\n" +
                "                value: 'Unknown'\n" +
                "            }, 1);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    for (x in propertiesNumbers) {\n" +
                "        property = propertiesNumbers[x];\n" +
                "        if (this[property] != null) {\n" +
                "            if (this[property].status !== 'CONFLICT') {\n" +
                "                var idx = Math.floor(this[property].values[0] / 10);\n" +
                "                emit({\n" +
                "                    property: property,\n" +
                "                    value: idx\n" +
                "                }, 1);\n" +
                "            } else {\n" +
                "                emit({\n" +
                "                    property: property,\n" +
                "                    value: 'CONFLICT'\n" +
                "                }, 1);\n" +
                "            }\n" +
                "        } else {\n" +
                "            emit({\n" +
                "                property: property,\n" +
                "                value: 'Unknown'\n" +
                "            }, 1);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    for (x in propertiesDate) {\n" +
                "        property = propertiesDate[x];\n" +
                "        if (this[property] != null && this[property].values[0] != undefined) {\n" +
                "            if (this[property].status !== 'CONFLICT') {\n" +
                "                var date = new Date(this[property].value);\n" +
                "                emit({\n" +
                "                    property: property,\n" +
                "                    value: date.getFullYear()\n" +
                "                }, 1);\n" +
                "            } else {\n" +
                "                emit({\n" +
                "                    property: property,\n" +
                "                    value: 'CONFLICT'\n" +
                "                }, 1);\n" +
                "            }\n" +
                "        } else {\n" +
                "            emit({\n" +
                "                property: property,\n" +
                "                value: 'Unknown'\n" +
                "            }, 1);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    for (x in propertyStats) {\n" +
                "        property = propertyStats[x];\n" +
                "        if (this[property] != null) {\n" +
                "            emit({\n" +
                "                property: property,\n" +
                "                value: property\n" +
                "            }, {\n" +
                "                sum: this[property].values[0],\n" +
                "                min: this[property].values[0],\n" +
                "                max: this[property].values[0],\n" +
                "                count: 1,\n" +
                "                diff: 0,\n" +
                "            });\n" +
                "        }\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "}";

        String reduce= "function reduce(key, values) {\n" +
                "    if (key.property != 'size') {\n" +
                "        var res = 0;\n" +
                "        values.forEach(function(v) {\n" +
                "            res += v;\n" +
                "        });\n" +
                "        return res;\n" +
                "    } else {\n" +
                "        var a = values[0];\n" +
                "        for (var i = 1; i < values.length; i++) {\n" +
                "            var b = values[i];\n" +
                "            var delta = a.sum / a.count - b.sum / b.count;\n" +
                "            var weight = (a.count * b.count) / (a.count + b.count);\n" +
                "            a.diff += b.diff + delta * delta * weight;\n" +
                "            a.sum += b.sum;\n" +
                "            a.count += b.count;\n" +
                "            a.min = Math.min(a.min, b.min);\n" +
                "            a.max = Math.max(a.max, b.max);\n" +
                "        }\n" +
                "        return a;\n" +
                "    }\n" +
                "}";

        String finalize="function finalize(key, value) {\n" +
                "  if (key.property == 'size' ){\n" +
                "    value.avg = value.sum / value.count;\n" +
                "    value.variance = value.diff / value.count;\n" +
                "    value.stddev = Math.sqrt(value.variance);\n" +
                "    return value;\n" +
                "  } return value;\n" +
                "}";
        DBObject query = this.getCachedFilter( filter );
        String propertiesString = ListToString(properties);
        map = map.replace("@1", ListToString(propertyStats));
        map = map.replace("@2", ListToString(propertiesDate));
        map = map.replace("@3", ListToString(propertiesNumbers));
        map = map.replace("@4", ListToString(propertiesOthers));
       // LOG.debug( "Executing histogram map reduce job with following map:\n{}", map );
        LOG.debug( "filter query is:\n{}", query );
        DBCollection elmnts = getCollection( Element.class );
        MapReduceCommand cmd = new MapReduceCommand( elmnts, map, reduce, null, INLINE, query );
        cmd.setFinalize(finalize);
        MapReduceOutput output = elmnts.mapReduce( cmd );
        List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get( "results" );
        LOG.debug( "MapReduce produced {} results", results.size() );
        DBCollection histCollection = this.db.getCollection( TBL_HISTOGRAMS );
        BasicDBObject old = new BasicDBObject( "_id", key );
        BasicDBObject res = new BasicDBObject( old.toMap() );
        res.put( "results", results );
        histCollection.update( old, res, true, false );

        DBCursor cursor = histCollection.find( new BasicDBObject( "_id", key ) );

        if ( cursor.count() == 0 ) {
            return null;
        }
        long end = System.currentTimeMillis();
        LOG.debug( "The map-reduce job took {} seconds", (end - start)/1000 );
        return (DBObject) cursor.next().get( "results" );




    }

    private String ListToString(List<String> properties) {
        String propertiesString="[";
        for (String p : properties){
            propertiesString += "'"+p +"'," ;
        }
        if (properties.size()>0)
            propertiesString = propertiesString.substring(0, propertiesString.length() - 1);
        propertiesString += "]";
        return propertiesString;
    }

    private DBObject histogramMapReduce( int key, List<String> properties, Filter filter ) {
        long start = System.currentTimeMillis();
        String map = "function() {\n" +
                "    var ar = @1;\n" +
                "    for (x in ar) {\n" +
                "        property = ar[x];\n" +
                "        if (this[property] != null) {\n" +
                "            if (this[property].status != 'CONFLICT') {\n" +
                "                emit({\n" +
                "                    property: property,\n" +
                "                    value: this[property].value\n" +
                "                }, 1);\n" +
                "            } else {\n" +
                "                emit({\n" +
                "                    property: property,\n" +
                "                    value: 'CONFLICT'\n" +
                "                }, 1);\n" +
                "            }\n" +
                "        } else {\n" +
                "            emit({\n" +
                "                property: property,\n" +
                "                value: 'Unknown'\n" +
                "            }, 1);\n" +
                "        }\n" +
                "    }\n" +
                "}";

        String reduce= "function(key, values) {\n" +
                "    var res = 0;\n" +
                "    values.forEach(function(v) {\n" +
                "        res += v;\n" +
                "    });\n" +
                "    return Array.sum(values);\n" +
                "}";
        DBObject query = this.getCachedFilter( filter );
        String propertiesString = ListToString(properties);
        map = map.replace("@1", propertiesString);
        LOG.debug( "Executing histogram map reduce job with following map:\n{}", map );
        LOG.debug( "filter query is:\n{}", query );
        DBCollection elmnts = getCollection( Element.class );
        MapReduceCommand cmd = new MapReduceCommand( elmnts, map, reduce, null, INLINE, query );

        MapReduceOutput output = elmnts.mapReduce( cmd );
        List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get( "results" );

        DBCollection histCollection = this.db.getCollection( TBL_HISTOGRAMS );
        BasicDBObject old = new BasicDBObject( "_id", key );
        BasicDBObject res = new BasicDBObject( old.toMap() );
        res.put( "results", results );
        histCollection.update( old, res, true, false );

        DBCursor cursor = histCollection.find( new BasicDBObject( "_id", key ) );

        if ( cursor.count() == 0 ) {
            return null;
        }
        long end = System.currentTimeMillis();
        LOG.debug( "The map-reduce job took {} seconds", (end - start)/1000 );
        return (DBObject) cursor.next().get( "results" );
    }

}