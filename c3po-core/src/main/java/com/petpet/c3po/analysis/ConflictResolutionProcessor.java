package com.petpet.c3po.analysis;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.petpet.c3po.adaptor.rules.ContentTypeIdentificationRule;
import com.petpet.c3po.analysis.conflictResolution.Rule;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.controller.Consolidator;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.utils.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        for (Rule r : rules) {
            persistence.update(r.getElement(), r.getFilter());
            Map<String, Object> map = persistence.getResult();
            if (map.get("count") != null)
                result += (int) map.get("count");
        }
       // updateContentType(filter);
        return result;
    }

    private void updateContentType(Filter filter) {
        ContentTypeIdentificationRule rule =new ContentTypeIdentificationRule();
        LinkedBlockingQueue<Element> queue=new LinkedBlockingQueue<Element>();
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();

        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        List<Consolidator> consolidators = new ArrayList<Consolidator>();
        for ( int i = 0; i < 2; i++ ) {
            Consolidator c = new Consolidator(persistence, queue);
            consolidators.add( c );
            threadPool.submit( c );
        }
        threadPool.shutdown();
        Iterator<Element> elementIterator = persistence.find(Element.class, filter);
        while (elementIterator.hasNext()){
            Element next = elementIterator.next();
            Element processed = rule.process(next);
            queue.add(processed);
        }
        for ( Consolidator c : consolidators ) {
            c.setRunning( false );
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

    public static long getConflictsCount(Filter filter){
        LOG.info("Calculating conflicts count");
        MongoPersistenceLayer persistence = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();
        String map2 = "function map() {\n" +
                "    if ((this['format'] != null && this['format'].status != null && this['format'].status == 'CONFLICT') ||\n" +
                "        (this['mimetype'] != null && this['mimetype'].status != null && this['mimetype'].status == 'CONFLICT') ||\n" +
                "        (this['format_version'] != null && this['format_version'].status != null && this['format_version'].status == 'CONFLICT')) {\n" +
                "        emit('CONFLICT', 1);\n" +
                "    }\n" +
                "   \n" +
                "}";

        String reduce = "function reduce(key, values) {" +
                "var res = 0;" +
                "values.forEach(function (v) {" +
                "res += v;" +
                "});" +
                "return res;" +
                "}";


        List<BasicDBObject> basicDBObjects = persistence.mapReduceRaw(map2, reduce, filter);
        if (basicDBObjects.size()==0){
            return 0;
        }
        BasicDBObject basicDBObject = basicDBObjects.get(0);
        Double conflictsDouble  = basicDBObject.getDouble("value");
        return conflictsDouble.longValue();
    }


    public static File printCSV(String path, String url, Filter filter) {
        LOG.info("Generating a csv file with conflict overview table");
        MongoPersistenceLayer persistence = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();
        String map2 = "function map() {\n" +
                "var result=null;\n" +
                "if (    (this['format'] != null && this['format'].status != null && this['format'].status == 'CONFLICT') || \n" +
                "(this['mimetype'] !=null && this['mimetype'].status != null && this['mimetype'].status == 'CONFLICT') || \n" +
                "(this['format_version'] !=null && this['format_version'].status != null && this['format_version'].status == 'CONFLICT') ) {\n" +
                "result={}; var format={};\n" +
                "if (this['format'] != null){\n" +
                "var format={};\n" +
                "format.status=this['format'].status;\n" +
                "if (this['format'].status == 'CONFLICT'){\n" +
                "format.values=this['format'].values;\n" +
                "} else{\n" +
                "format.values=[this['format'].value];\n" +
                "}\n" +
                "format.sources=this['format'].sources;\n" +
                "} \n" +
                "result.format=format;\n" +
                "var format_version={};\n" +
                "if (this['format_version'] !=null) {\n" +
                "format_version.status=this['format_version'].status;\n" +
                "if (this['format_version'].status == 'CONFLICT'){\n" +
                "format_version.values=this['format_version'].values;\n" +
                "} else{\n" +
                "format_version.values=[this['format_version'].value];\n" +
                "}\n" +
                "format_version.sources=this['format_version'].sources;\n" +
                "}\n" +
                "result.format_version=format_version;\n" +
                "var mimetype={};\n" +
                "if (this['mimetype'] !=null ){\n" +
                "mimetype.status=this['mimetype'].status;\n" +
                "if (this['mimetype'].status == 'CONFLICT'){\n" +
                "mimetype.values=this['mimetype'].values;\n" +
                "} else{\n" +
                "mimetype.values=[this['mimetype'].value];\n" +
                "}\n" +
                "mimetype.sources=this['mimetype'].sources;\n" +
                "}\n" +
                "result.mimetype=mimetype;\n" +
                "}\n" +
                "if (result!=null)  {\n" +
                "emit(result,1);\n" +
                "}    \n" +
                "}    ";

        String reduce = "function reduce(key, values) {" +
                "var res = 0;" +
                "values.forEach(function (v) {" +
                "res += v;" +
                "});" +
                "return res;" +
                "}";

        Iterator<Property> propertyIterator = persistence.find(Property.class, null);
        List<String> properties = new ArrayList<>();
        while (propertyIterator.hasNext()) {
            Property next = propertyIterator.next();
            if (!properties.contains(next.getKey()))
                properties.add(next.getKey());
        }

        Iterator<Source> sourceIterator = persistence.find(Source.class, null);
        List<String> sources = new ArrayList<>();
        String header = "";
        while (sourceIterator.hasNext()) {
            Source next = sourceIterator.next();
            sources.add(next.getName() + ":" + next.getVersion());
        }
        header += "Count;";
        for (String source : sources)
            header += "Format-" + source + ";";

        for (String source : sources)
            header += "FormatVersion-" + source + ";";

        for (String source : sources)
            header += "Mimetype-" + source + ";";

        header += "SampleURL;Query";

        List<BasicDBObject> basicDBObjects = persistence.mapReduceRaw(map2, reduce, filter);

        int size = basicDBObjects.size();
        System.out.print(size);
        List<BasicDBObject> result = new ArrayList<>();

        File file = new File(path);

        // if file doesnt exists, then create it
        PrintWriter out = null;
        try {
            if (!file.createNewFile()) {
                file.delete();
                file.createNewFile();
            }
            out = new PrintWriter(file);
        } catch (IOException e) {
            LOG.error("An error occured during csv generation {}", e.getMessage());
        }

        out.println(header);
        for (BasicDBObject obj : basicDBObjects) {

                String output = "";
                Double count = obj.getDouble("value");
                output += count.intValue();
                BasicDBObject id1 = (BasicDBObject) obj.get("_id");
                if (id1.size() == 0) continue;
                BasicDBObject format = (BasicDBObject) id1.get("format");
                BasicDBObject format_version = (BasicDBObject) id1.get("format_version");
                BasicDBObject mimetype = (BasicDBObject) id1.get("mimetype");

                BasicDBList format_values = (BasicDBList) format.get("values");
                BasicDBList format_sources = (BasicDBList) format.get("sources");
                output += basicDBListsToCSV(format_values, format_sources, sources);

                BasicDBList format_version_values = (BasicDBList) format_version.get("values");
                BasicDBList format_version_sources = (BasicDBList) format_version.get("sources");
                output += basicDBListsToCSV(format_version_values, format_version_sources, sources);

                BasicDBList mimetype_values = (BasicDBList) mimetype.get("values");
                BasicDBList mimetype_sources = (BasicDBList) mimetype.get("sources");
                output += basicDBListsToCSV(mimetype_values, mimetype_sources, sources);

                BasicDBList andQuery = new BasicDBList();
                BasicDBObject query;

                String getQuery = "";

                if (format.size() > 0) {
                    query = new BasicDBObject();
                    if (format.getString("status").equals("CONFLICT")) {
                        query.put("format.values", format_values);
                        for (Object o : format_values) {
                            getQuery += "format=" + o.toString() + "&";
                        }
                    } else {
                        query.put("format.value", format_values.get(0));
                        getQuery += "format=" + format_values.get(0).toString() + "&";
                    }
                    andQuery.add(query);
                }

                if (format_version.size() > 0) {
                    query = new BasicDBObject();
                    if (format_version.getString("status").equals("CONFLICT")) {
                        query.put("format_version.values", format_version_values);
                        for (Object o : format_version_values) {
                            getQuery += "format_version=" + o.toString() + "&";
                        }
                    } else {
                        query.put("format_version.value", format_version_values.get(0));
                        getQuery += "format_version=" + format_version_values.get(0).toString() + "&";
                    }
                    andQuery.add(query);
                }

                if (mimetype.size() > 0) {
                    query = new BasicDBObject();
                    if (mimetype.getString("status").equals("CONFLICT")) {
                        query.put("mimetype.values", mimetype_values);
                        for (Object o : mimetype_values) {
                            getQuery += "mimetype=" + o.toString() + "&";
                        }
                    } else {
                        query.put("mimetype.value", mimetype.get(0));
                        getQuery += "mimetype=" + mimetype_values.get(0).toString() + "&";
                    }
                    getQuery = getQuery.substring(0, getQuery.length() - 1);
                    getQuery = getQuery.replace("+", "%2B").replace(" ", "%20");
                    andQuery.add(query);
                }
                query = new BasicDBObject("$and", andQuery);
                Iterator<Element> elementIterator = persistence.findQ(Element.class, query);
                output += ";";
                if (elementIterator.hasNext()) {
                    Element next = elementIterator.next();
                    output += "http://" + url + "/c3po/objects/" + next.getId();
                }
                output += ";" + "http://" + url + "/c3po/overview/filter?" + getQuery + "&template=Conflict";
                output += ";" + "http://" + url + "/c3po/export/csv/filter?" + getQuery;
                out.println(output);
        }
        out.close();
        return file;
    }

    public static String basicDBListsToCSV(BasicDBList value, BasicDBList source, List<String> sources) {

        String[] result = new String[sources.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = "";
        if (value != null && source != null) {
            int size = value.size();
            if (size > 1) {
                for (int i = 0; i < value.size(); i++) {
                    String valueConflicted = value.get(i).toString();
                    Integer sourceID = Integer.parseInt(source.get(i).toString());
                    result[sourceID] = valueConflicted;
                }
            } else {
                String valueConflicted = value.get(0).toString();
                for (int i = 0; i < source.size(); i++) {
                    Integer sourceID = Integer.parseInt(source.get(i).toString());
                    result[sourceID] = valueConflicted;
                }
            }
        }
        String output = "";
        for (int i = 0; i < result.length; i++)
            output += ";" + result[i];
        return output;
    }

}


