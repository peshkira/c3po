package com.petpet.c3po.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.mongodb.*;
import com.petpet.c3po.adaptor.rules.ContentTypeIdentificationRule;
import com.petpet.c3po.analysis.conflictResolution.Rule;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.filtering.PropertyFilterCondition;
import com.petpet.c3po.controller.Consolidator;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.utils.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by artur on 31/03/16.
 */
public class ConflictResolutionProcessor {

    /**
     * Default logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SizeRepresentativeGenerator.class);

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    List<Rule> rules;

    public long resolve(Filter filter) {
        long result = 0;
        MongoPersistenceLayer persistence = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();
        for (Rule r : rules) {
            Iterator<Element> elementIterator = persistence.find(Element.class, r.getFilter());
            Element sample = elementIterator.next();

            List<String> properties = new ArrayList<>();
            for (MetadataRecord metadataRecord : sample.getMetadata()) {
                properties.add(metadataRecord.getProperty());
            }

            List<MetadataRecord> metadataRecords=assemblyRecords(r.getMetadataRecordList());
            for (MetadataRecord metadataRecord : metadataRecords) {
                BasicDBObject object=serialize(metadataRecord,properties, sample);
                persistence.update(object, r.getFilter());
            }


            Map<String, Object> map = persistence.getResult();
            if (map.get("count") != null)
                result +=(long) map.get("count");
        }
        // updateContentType(filter);
        return result;
    }

    private BasicDBObject serialize(MetadataRecord metadataRecordToDelete, List<String> properties, Element sample) {
        MongoPersistenceLayer persistence = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();
        BasicDBObject result=new BasicDBObject();

        for (MetadataRecord metadataRecord : sample.getMetadata()) {
            if (metadataRecord.getProperty().equals(metadataRecordToDelete.getProperty())){
                for (String s : metadataRecordToDelete.getSourcedValues().keySet()) {
                    metadataRecord.getSourcedValues().remove(s);
                }
                BasicDBList sourcedValuesList = new BasicDBList();
                for (Map.Entry<String, String> stringStringEntry : metadataRecord.getSourcedValues().entrySet()) {
                    String key = stringStringEntry.getKey();
                    String value = stringStringEntry.getValue();
                    BasicDBObject sourcedvalue=new BasicDBObject();
                    sourcedvalue.append("source", key);
                    sourcedvalue.append("value", value);
                    sourcedValuesList.add(sourcedvalue);
                }
                result.append("metadata."+properties.indexOf(metadataRecord.getProperty())+".sourcedValues", sourcedValuesList);

                if (metadataRecordToDelete.getStatus().equals("RESOLVED")){
                    result.append("metadata."+properties.indexOf(metadataRecord.getProperty())+".status", metadataRecordToDelete.getStatus());
                }
                break;
            }
        }


        return result;
    }

    private List<MetadataRecord> assemblyRecords(List<MetadataRecord> metadataRecords) {
        List<MetadataRecord> result=new ArrayList<MetadataRecord>();
        List<String> processedProperties=new ArrayList<String>();
        for (MetadataRecord metadataRecord : metadataRecords) {
            if (!processedProperties.contains(metadataRecord.getProperty())) {
                processedProperties.add(metadataRecord.getProperty());
                List<MetadataRecord> tmpRecords = getAllRecordsWithProperty(metadataRecord.getProperty(), metadataRecords);
                MetadataRecord tmprecord = combineRecords(tmpRecords);
                result.add(tmprecord);
            }
        }
        return result;
    }

    private MetadataRecord combineRecords(List<MetadataRecord> tmpRecords) {
        MetadataRecord result=new MetadataRecord();
        for (MetadataRecord tmpRecord : tmpRecords) {
            if (tmpRecord.getProperty()!=null && result.getProperty()==null){
                result.setProperty(tmpRecord.getProperty());
            }
            if (tmpRecord.getStatus().equals("RESOLVED")){
                result.setStatus(tmpRecord.getStatus());
            }
            if (!tmpRecord.getSourcedValues().isEmpty()){
                result.getSourcedValues().putAll(tmpRecord.getSourcedValues());
            }
        }
        return result;
    }

    private List<MetadataRecord> getAllRecordsWithProperty(String property, List<MetadataRecord> metadataRecords) {
        List<MetadataRecord> result=new ArrayList<MetadataRecord>();
        for (MetadataRecord metadataRecord : metadataRecords) {
            if (metadataRecord.getProperty().equals(property)) {
                result.add(metadataRecord);
            }
        }
        return result;
    }


    public long resolve(Filter filter, Map<String, String> resolutions) {
        long result = 0;
        MongoPersistenceLayer persistence = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();
        DB db = persistence.getDb();
        DBObject cachedFilter = persistence.getCachedFilter(filter);

        LOG.debug("Applying the filter query:");
        LOG.debug(cachedFilter.toString());
        DBCollection elements = db.getCollection("elements");

        long countBeforeUpdate = elements.count(cachedFilter);

        /***
         *
         db.elements.update({ _id : ObjectId("5899f1c8fc4e9a2170fadff4")},
         {$pull: {metadata: {property: "format", status: "CONFLICT"}}})

         db.elements.update({ _id : ObjectId("5899f1c8fc4e9a2170fadff4")},
         {$addToSet: {metadata:{property:"format" ,status: "RESOLVED", sourcedValues:[{source:"C3PO:0.6", value: "abracadabra"}]}}} )
         *
         */
        WriteResult writeResult = null;
        for (Map.Entry<String, String> stringStringEntry : resolutions.entrySet()) {

            String propertyToResolve = stringStringEntry.getKey();


//First, the new metadata records are added
            String resolveTo = stringStringEntry.getValue();
            BasicDBObject metadata2 = new BasicDBObject("property", propertyToResolve);
            metadata2.append("status", "RESOLVED");
            BasicDBList sourcedValues = new BasicDBList();
            String sourceC3PO = persistence.getCache().getSource("C3PO", "0.6").toString();
            BasicDBObject sourcedValue = new BasicDBObject("source", sourceC3PO);
            sourcedValue.append("value", resolveTo);
            sourcedValues.add(sourcedValue);
            metadata2.append("sourcedValues", sourcedValues);
            BasicDBObject addToSet = new BasicDBObject("metadata", metadata2);
            BasicDBObject update2 = new BasicDBObject("$addToSet", addToSet);
            LOG.debug("Applying the update:");
            LOG.debug(update2.toString());
            writeResult = elements.update(cachedFilter, update2, true, true);
            if (writeResult != null)
                LOG.debug("The query affected the following number of objects: " + writeResult.toString());
        }

        //Second, the metadata records wih conflicts are removed

        BasicDBObject metadata = new BasicDBObject("status", "CONFLICT");


        BasicDBList properties = new BasicDBList();
        for (Map.Entry<String, String> stringStringEntry : resolutions.entrySet()) {
            String propertyToResolve = stringStringEntry.getKey();
            properties.add(propertyToResolve);
        }

        BasicDBObject in = new BasicDBObject("$in", properties);
        metadata.append("property", in);
        BasicDBObject pull = new BasicDBObject("metadata", metadata);
        BasicDBObject update = new BasicDBObject("$pull", pull);
        LOG.debug("Applying the update:");
        LOG.debug(update.toString());
        WriteResult update1 = elements.update(cachedFilter, update, false, true);
        long countAfterUpdate = elements.count(cachedFilter);
        result = countBeforeUpdate - countAfterUpdate;
        LOG.debug("The query affected the following number of objects: " + result);
        return result;
    }

    private void updateContentType(Filter filter) {
        ContentTypeIdentificationRule rule = new ContentTypeIdentificationRule();
        LinkedBlockingQueue<Element> queue = new LinkedBlockingQueue<Element>();
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();

        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        List<Consolidator> consolidators = new ArrayList<Consolidator>();
        for (int i = 0; i < 2; i++) {
            Consolidator c = new Consolidator(persistence, queue);
            consolidators.add(c);
            threadPool.submit(c);
        }
        threadPool.shutdown();
        Iterator<Element> elementIterator = persistence.find(Element.class, filter);
        while (elementIterator.hasNext()) {
            Element next = elementIterator.next();
            Element processed = rule.process(next);
            queue.add(processed);
        }
        for (Consolidator c : consolidators) {
            c.setRunning(false);
        }

    }


    public void calculateStats() {
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        Iterator<Element> elementIterator = persistence.find(Element.class, null);
        Iterator<Source> sourceIterator = persistence.find(Source.class, null);
        List<String> sources = new ArrayList<>();
        while (sourceIterator.hasNext()) {
            Source next = sourceIterator.next();
            if (!sources.contains(next.getName()))
                sources.add(next.getName());
        }
        while (elementIterator.hasNext()) {
            Element next = elementIterator.next();
            List<MetadataRecord> metadata = next.getMetadata();
            for (MetadataRecord mr : metadata) {
                if (mr.getStatus().equals("CONFLICT")) {
                    List<String> mrValues = mr.getValues();
                    List<String> mrSources = mr.getSources();
                }
            }
        }
    }

    public static long getConflictsCount(Filter filter) {
        LOG.info("Calculating conflicts count");
        List<String> properties = new ArrayList<String>();
        properties.add("format");
        properties.add("format_version");
        properties.add("mimetype");

        long result = getConflictsCount(filter, properties);

        return result;
    }

    private static long getConflictsCount(Filter filter, List<String> properties) {
        MongoPersistenceLayer persistence = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();
        JsonNode node = new POJONode(properties);
        String s = "[";
        for (String property : properties) {
            s += "'" + property + "',";
        }
        s = s.substring(0, s.length() - 1);
        s += "]";
        //String s = properties.toString();
        //s = node.toString();
        String map = "function map() {\n" +
                "    properties = " + s + ";\n" +
                "    for (mr in this.metadata){\n" +
                "        metadataRecord=this.metadata[mr];\n" +
                "        for (p in properties){\n" +
                "            property = properties[p];\n" +
                "            if(metadataRecord.property == property){\n" +
                "                if (metadataRecord.status == 'CONFLICT'){\n" +
                "                    emit('CONFLICT', 1);\n" +
                "                    return;\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        String reduce = "function reduce(key, values) {" +
                "var res = 0;" +
                "values.forEach(function (v) {" +
                "res += v;" +
                "});" +
                "return res;" +
                "}";


        List<BasicDBObject> basicDBObjects = persistence.mapReduceRaw(map, reduce, filter);
        if (basicDBObjects.size() == 0) {
            return 0;
        }
        BasicDBObject basicDBObject = basicDBObjects.get(0);
        Double conflictsDouble = basicDBObject.getDouble("value");
        return conflictsDouble.longValue();
    }

    public static Map<String, Integer> getOverview(String url, Filter filter) {
        LOG.info("Generating a JSON file with conflict overview table");
        Map<String, Integer> result = new HashMap<String, Integer>();

        List<String> properties = new ArrayList<String>();
        properties.add("format");
        properties.add("format_version");
        properties.add("mimetype");

        List<BasicDBObject> basicDBObjects = getOverview(filter, properties);

        int size = basicDBObjects.size();

        for (BasicDBObject obj : basicDBObjects) {
            Double count = obj.getDouble("value");
            BasicDBObject id1 = (BasicDBObject) obj.get("_id");
            if (id1.size() == 0) continue;
            Filter f = new Filter();
            for (String property : properties) {
                PropertyFilterCondition pfc = getFilterCondition(property, id1);
                pfc.setStrict(true);
                f.getPropertyFilterConditions().add(pfc);
            }
            String query = f.toSRUString();
            query = query.replace("+", "%2B").replace(" ", "%20");

            String link = "http://" + url + "/c3po/overview/filter?" + query + "&template=Conflict";

            result.put(link, count.intValue());

        }
        result = sortByValues(result);
        return result;

    }

    private static PropertyFilterCondition getFilterCondition(String property, BasicDBObject id1) {
        PropertyFilterCondition pfc = new PropertyFilterCondition();

        BasicDBObject propertyObj = (BasicDBObject) id1.get(property);
        if (propertyObj == null)
            return pfc;

        pfc.setProperty(property);
        String status = propertyObj.getString("status");
        if (status != null)
            pfc.getStatuses().add(status);
        BasicDBList sourcedValues = (BasicDBList) propertyObj.get("sourcedValues");
        if (sourcedValues != null && sourcedValues.size() > 0) {
            for (Object obj : sourcedValues) {
                BasicDBObject sourcedValue = (BasicDBObject) obj;
                String source = sourcedValue.getString("source");
                String value = sourcedValue.getString("value");
                pfc.getSourcedValues().put(source, value);
            }
        }
        return pfc;
    }

    public static List<BasicDBObject> getOverview(Filter filter, List<String> properties) {
        MongoPersistenceLayer persistence = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();

        String s = "[";
        for (String property : properties) {
            s += "'" + property + "',";
        }
        s = s.substring(0, s.length() - 1);
        s += "]";

        String map = "function map() {\n" +
                "    var result={};\n" +
                "    var conflicted=false;\n" +
                "    properties = " + s + ";\n" +
                "    for (mr in this.metadata){\n" +
                "        metadataRecord=this.metadata[mr];\n" +
                "        for (p in properties){\n" +
                "            property = properties[p];\n" +
                "            if(metadataRecord.property == property){\n" +
                "                result[property]={};\n" +
                "                result[property].status=metadataRecord.status;\n" +
                "                result[property].sourcedValues=new Array();\n" +
                "                for(var source in metadataRecord.sourcedValues){\n" +
                "                    result[property].sourcedValues.push(metadataRecord.sourcedValues[source]);\n" +
                "                }\n" +
                "                if (metadataRecord.status == 'CONFLICT'){\n" +
                "                    conflicted=true;\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    if (conflicted){\n" +
                "        emit(result,1);\n" +
                "    }\n" +
                "}";

        String reduce = "function reduce(key, values) {" +
                "var res = 0;" +
                "values.forEach(function (v) {" +
                "res += v;" +
                "});" +
                "return res;" +
                "}";

        List<BasicDBObject> basicDBObjects = persistence.mapReduceRaw(map, reduce, filter);

        return basicDBObjects;

    }

    private static List<String> getAllProperties(MongoPersistenceLayer persistence) {
        Iterator<Property> propertyIterator = persistence.find(Property.class, null);
        List<String> properties = new ArrayList<>();
        while (propertyIterator.hasNext()) {
            Property next = propertyIterator.next();
            if (!properties.contains(next.getKey()))
                properties.add(next.getKey());
        }
        return properties;
    }

    private static Map<String, String> getSourcedValues(BasicDBList sourcedValues, List<String> sources) {
        Map<String, String> result = new HashMap<>();

        if (sourcedValues != null) {
            for (int i = 0; i < sourcedValues.size(); i++) {
                String valueConflicted = ((BasicDBObject) sourcedValues.get(i)).get("value").toString();
                String source = ((BasicDBObject) sourcedValues.get(i)).get("source").toString();
                result.put(source, valueConflicted);
            }
        }
        return result;
    }


    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator = new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) return 1;
                else return compare;
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    static PersistenceLayer persistenceLayer = Configurator.getDefaultConfigurator().getPersistence();

    public static String basicDBListsToCSV(BasicDBList sourcedValues, List<String> sources) {

        String[] result = new String[sources.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = "";
        if (sourcedValues != null) {
            for (int i = 0; i < sourcedValues.size(); i++) {
                String valueConflicted = ((BasicDBObject) sourcedValues.get(i)).get("value").toString();
                //  Integer sourceID = Integer.parseInt(source.get(i).toString());
                //  Source source1 = persistenceLayer.getCache().getSource(source.get(i).toString());
                String sourceString = ((BasicDBObject) sourcedValues.get(i)).get("source").toString();
                result[sources.indexOf(sourceString)] = valueConflicted;
            }
        }
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        for (int i = 0; i < result.length; i++)
            sb.append(" , " + result[i]);
        return sb.toString();
    }

    public String printCSV(String url, Filter filter, Collection<String> props) {

        LOG.info("Generating a csv file with conflict overview table");
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        MongoPersistenceLayer persistence = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();
        sb.append("Count , Property , ");
        for (String source : getAllSources(persistence))
            sb.append(source + " , ");

        sb.append( "Query , Export\n");

        Map<String, Integer> stringDoubleMap = new HashMap<String, Integer>();
        for (String prop : props) {
            Collection<? extends BasicDBObject> conflictsForProperty = getConflictsForProperty(filter, prop);
            stringDoubleMap.putAll(basicDBObjectsToCSV((List<BasicDBObject>) conflictsForProperty, prop, url));
        }
        stringDoubleMap = sortByValues(stringDoubleMap);
        for (String s : stringDoubleMap.keySet()) {
            sb.append(s + "\n");
        }

        return sb.toString();
    }

    private Map<String, Integer> basicDBObjectsToCSV(List<BasicDBObject> basicDBObjects, String prop, String url) {
        MongoPersistenceLayer persistence = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();

        List<String> sources = getAllSources(persistence);

        Map<String, Integer> result = new HashMap<String, Integer>();

        for (BasicDBObject obj : basicDBObjects) {
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            Filter filter_tmp = new Filter();
            Integer count = obj.getInt("value");
            sb.append(count.intValue());
            sb.append(" , " + prop);
            BasicDBObject list = (BasicDBObject) obj.get("_id");
            BasicDBObject property = (BasicDBObject) list.get(prop);
            if (property != null && property.get("sourcedValues") != null) {
                BasicDBList sourcedValues = (BasicDBList) property.get("sourcedValues");
                sb.append(basicDBListsToCSV(sourcedValues, sources));
                PropertyFilterCondition pfc = new PropertyFilterCondition();
                pfc.setStrict(true);
                pfc.addCondition(PropertyFilterCondition.PropertyFilterConditionType.PROPERTY, prop);
                pfc.setSourcedValues(getSourcedValues(sourcedValues, sources));
                List<String> statuses=new ArrayList<String>();
                statuses.add("CONFLICT");
                pfc.setStatuses(statuses);
                filter_tmp.getPropertyFilterConditions().add(pfc);
            }

            String s = filter_tmp.toSRUString();

            // Iterator<Element> elementIterator = persistence.find(Element.class, filter_tmp);
            //  sb.append( ",");
            //  if (elementIterator.hasNext()) {
            //      Element next = elementIterator.next();
            //      sb.append( "http://" + url + "/c3po/objects/" + next.getId());
            //  }
            sb.append(" , " + "http://" + url + "/c3po/overview/filter?" + s + "&template=Conflict");
            sb.append(" , " + "http://" + url + "/c3po/export/csv/filter?" + s);

            result.put(sb.toString(), count);

        }
        return result;

    }

    private List<String> getAllSources(MongoPersistenceLayer persistence) {
        Iterator<Source> sourceIterator = persistence.find(Source.class, null);
        List<String> sources = new ArrayList<>();
        while (sourceIterator.hasNext()) {
            Source next = sourceIterator.next();
            sources.add(next.toString());
        }
        return sources;
    }

    private Collection<? extends BasicDBObject> getConflictsForProperty(Filter filter, String prop) {
        MongoPersistenceLayer persistence = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();

        String map = "function map() {\n" +
                "    var result={};\n" +
                "    var conflicted=false;\n" +
                "    property ='" + prop + "';\n" +
                "    for (mr in this.metadata){\n" +
                "        metadataRecord=this.metadata[mr];\n" +
                "        if(metadataRecord.property == property){\n" +

                "            if (metadataRecord.status == 'CONFLICT'){\n" +
                "                conflicted=true;\n" +
                "                result[property]={};\n" +
                "                result[property].status=metadataRecord.status;\n" +
                "                result[property].sourcedValues=new Array();\n" +
                "                for(var source in metadataRecord.sourcedValues){\n" +
                "                   result[property].sourcedValues.push(metadataRecord.sourcedValues[source]);\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    if (conflicted){\n" +
                "        emit(result,1);\n" +
                "    }\n" +
                "}";

        String reduce = "function reduce(key, values) {" +
                "var res = 0;" +
                "values.forEach(function (v) {" +
                "res += v;" +
                "});" +
                "return res;" +
                "}";

        List<BasicDBObject> basicDBObjects = persistence.mapReduceRaw(map, reduce, filter);

        return basicDBObjects;

    }
}


