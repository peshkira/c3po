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

import java.net.UnknownHostException;
import java.util.*;

import com.mongodb.*;
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

    public Map<String, Long> parseMapReduce(DBObject object, String property){

        Map<String, Long> histogram=new HashMap<String, Long>();

        if ( object == null ) {
            return histogram;
        }

        List<BasicDBObject> results = (List<BasicDBObject>) object;
        for ( final BasicDBObject dbo : results ) {

            String p =  ((BasicDBObject) dbo.get("_id")).getString("property");
            String val = ((BasicDBObject) dbo.get("_id")).getString("value");
            try{
                long count = dbo.getLong("value");
                String type = getCache().getProperty(p).getType();
                if (type.equals(PropertyType.DATE.toString()))
                {
                    try {
                        Double v = Double.parseDouble(val);
                        int i = v.intValue();
                        histogram.put(String.valueOf(i), count);
                    }
                    catch (NumberFormatException nfe){
                        histogram.put(val, count);
                    }
                } else
                    histogram.put(val, count);
            } catch (Exception e){
                BasicDBObject v = (BasicDBObject) dbo.get("value");
                long sum, min, max, avg, std, var, count;
                try {
                    count = v.getLong("count");
                } catch (Exception ex) {
                    count = 0;
                }
                try {
                    sum = v.getLong("sum");
                } catch (Exception ex) {
                    sum = 0;
                }
                try {
                    min = v.getLong("min");
                } catch (Exception ex) {
                    min = 0;
                }
                try {
                    max = v.getLong("max");
                } catch (Exception ex) {
                    max = 0;
                }
                try {
                    avg = v.getLong("avg");
                } catch (Exception ex) {
                    avg = 0;
                }
                try {
                    std = v.getLong("stddev");
                } catch (Exception ex) {
                    std = 0;
                }
                try {
                    var = v.getLong("variance");
                } catch (Exception ex) {
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
        return histogram;
    }
    @Override
    public <T extends Model> Map<String, Map<String, Long>> getHistograms(List<String> properties, Filter filter, Map<String, List<Integer>> binThresholds){
        Map<String, Map<String, Long>> histograms=new HashMap<String, Map<String, Long>>();
        if (binThresholds==null){
            binThresholds=new HashMap<String, List<Integer>>();
        }
        for (String property : properties) {
            DBObject dbObject=null;
            dbObject = mapReduce(0, property, filter, binThresholds.get(property));
            Map<String, Long> stringLongMap = parseMapReduce(dbObject, property);
            histograms.put(property,stringLongMap);
        }
        return histograms;
    }


    @Override
    public <T extends Model> Map<String, Map<String, Long>> getStats(List<String> properties, Filter filter, Map<String, List<Integer>> binThresholds){
        Map<String, Map<String, Long>> histograms=new HashMap<String, Map<String, Long>>();
        for (String property : properties) {
            DBObject dbObject=null;
            dbObject = mapReduceStats(0, property, filter);
            Map<String, Long> stringLongMap = parseMapReduce(dbObject, property);
            histograms.put(property,stringLongMap);
        }
        return histograms;
    }

    public DBObject mapReduce(int key, String property, Filter filter, List<Integer> bins){
        LOG.debug( "Starting mapReduce for the following property: {}", property );
        long start = System.currentTimeMillis();
        Property prop = getCache().getProperty(property);
        String propType = prop.getType();
        String map="";
        String reduce="";
        if (propType.equals(PropertyType.STRING.toString())){
            map="function() {\n" +
                    "    property = '"+property+"';\n" +
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
                    "    }";
            reduce="function reduce(key, values) {\n" +
                    "    var res = 0;\n" +
                    "    values.forEach(function(v) {\n" +
                    "        res += v;\n" +
                    "    });\n" +
                    "    return res;\n" +
                    "}";

        } else if  (propType.equals(PropertyType.INTEGER.toString()) || propType.equals(PropertyType.FLOAT.toString())) {
           /* map="function() {\n" +
                    "    property = '"+property+"';\n" +
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
                    "    }";*/


           map="function() {\n" +
                   "    property = '"+property+"';\n" +
                   "    thresholds = "+ getBinThresholds(bins)+";\n" +
                   "        if (this[property] != null) {\n" +
                   "            if (this[property].status != 'CONFLICT') {\n" +
                   "                var val=this[property].values[0];\n" +
                   "                var skipped=false;\n" +
                   "                if (thresholds.length > 0)\n" +
                   "                    for (t in thresholds){\n" +
                   "                        threshold = thresholds[t];  \n" +
                   "                        if (val>=threshold[0] && val<=threshold[1]){\n" +
                   "                             emit({\n" +
                   "                                property: property,\n" +
                   "                                value: threshold[0]+'-'+threshold[1]\n" +
                   "                            }, 1);\n" +
                   "                             skipped=true;\n" +
                   "                             return;\n" +
                   "                         }\n" +
                   "\n" +
                   "                    }\n" +
                   "                if (!skipped)\n" +
                   "                    emit({\n" +
                   "                        property: property,\n" +
                   "                        value: val\n" +
                   "                    }, 1);\n" +
                   "\n" +
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
                   "    }";

           /* map="function() {\n" +
                    "    property = '"+property+"';\n" +
                    "    thresholds = '"+ getBinThresholds(bins)+"';\n" +
                    "        if (this[property] != null) {\n" +
                    "            if (this[property].status != 'CONFLICT') {\n" +
                    "                var val=this[property].values[0];\n" +
                    "                var skipped=false;\n" +
                    "                if (thresholds!=[])\n" +
                    "                    for (t in thresholds){\n" +
                    "                        threshold = thresholds[t];  \n" +
                    "                        if (val>=threshold[0] && val<threshold[1]){\n" +
                    "                             emit({\n" +
                    "                                property: property,\n" +
                    "                                value: threshold[1]\n" +
                    "                            }, 1);\n" +
                    "                             skipped=true;\n" +
                    "                             break;\n" +
                    "                         }\n" +
                    "\n" +
                    "                    }\n" +
                    "                if (!skipped)\n" +
                    "                    emit({\n" +
                    "                        property: property,\n" +
                    "                        value: val\n" +
                    "                    }, 1);\n" +
                    "\n" +
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
                    "    }";

*/
            reduce="function reduce(key, values) {\n" +
                    "    var res = 0;\n" +
                    "    values.forEach(function(v) {\n" +
                    "        res += v;\n" +
                    "    });\n" +
                    "    return res;\n" +
                    "}";

        } else if  (propType.equals(PropertyType.DATE.toString())) {
            map="function() {\n" +
                    "    property = '"+property+"';\n" +
                    "        if (this[property] != null) {\n" +
                    "            if (this[property].status != 'CONFLICT') {\n" +
                    "                var date = new Date(this[property].values[0]);" +
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
                    "    }";
            reduce="function reduce(key, values) {\n" +
                    "    var res = 0;\n" +
                    "    values.forEach(function(v) {\n" +
                    "        res += v;\n" +
                    "    });\n" +
                    "    return res;\n" +
                    "}";


        } else if  (propType.equals(PropertyType.BOOL.toString())) {
            map="function() {\n" +
                    "    property = '"+property+"';\n" +
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
                    "    }";
            reduce="function reduce(key, values) {\n" +
                    "    var res = 0;\n" +
                    "    values.forEach(function(v) {\n" +
                    "        res += v;\n" +
                    "    });\n" +
                    "    return res;\n" +
                    "}";
        }
        DBObject query = this.getCachedFilter( filter );
        LOG.debug( "Filter query is:\n{}", query );
        String queryString=query.toString();
        DBCollection elmnts = getCollection( Element.class );
        MapReduceCommand cmd = new MapReduceCommand( elmnts, map, reduce, null, INLINE, query );
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
        LOG.debug( "MapReduce took {} seconds", (end - start)/1000 );
        return (DBObject) cursor.next().get( "results" );
    }

    private String getBinThresholds(List<Integer> binThresholds) {
        if (binThresholds==null)
            return "[]";
        String result="[";
        for (int i = 0; i < binThresholds.size(); i++) {
            String threshold="[";
            if (i==0){
                threshold +="0," + (binThresholds.get(i)-1);
            } else {
                threshold +=binThresholds.get(i-1)+","+(binThresholds.get(i)-1);
            }
            threshold+="]";
            result+=threshold+",";
        }
        if (binThresholds.size()>0)
            result = result.substring(0, result.length() - 1);
        result += "]";
        return result;
    }


    public DBObject mapReduceStats(int key, String property, Filter filter){
        LOG.debug( "Starting mapReduceStats for the following property: {}", property );
        long start = System.currentTimeMillis();
        Property prop = getCache().getProperty(property);
        String propType = prop.getType();
        String map="";
        String reduce="";
        String finalize="";
        if  (propType.equals(PropertyType.INTEGER.toString()) || propType.equals(PropertyType.FLOAT.toString())) {
            map="function() {\n" +
                    "    property = '"+property+"';\n" +
                    " if (this[property] != null) {\n" +
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
                    "        }" +
                    "    }";
            reduce="function reduce(key, values) {\n" +
                    "var a = values[0];\n" +
                    "        for (var i = 1; i < values.length; i++) {\n" +
                    "            var b = values[i];\n" +
                    "            var delta = a.sum / a.count - b.sum / b.count;\n" +
                    "            var weight = (a.count * b.count) / (a.count + b.count);\n" +
                    "            a.diff += b.diff + delta * delta * weight;\n" +
                    "            a.sum = b.sum*1+ a.sum*1;\n" +
                    "            a.count += b.count;\n" +
                    "            a.min = Math.min(a.min, b.min);\n" +
                    "            a.max = Math.max(a.max, b.max);\n" +
                    "        }\n" +
                    "return a;" +
                    "}";
            finalize="function finalize(key, value) {\n" +
                    "    value.avg = value.sum / value.count;\n" +
                    "    value.variance = value.diff / value.count;\n" +
                    "    value.stddev = Math.sqrt(value.variance);\n" +
                    "    return value;\n" +
                    "}";

        }
        DBObject query = this.getCachedFilter( filter );
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


}