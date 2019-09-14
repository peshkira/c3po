package com.petpet.c3po.analysis;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.opencsv.CSVWriter;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.api.model.helper.filtering.PropertyFilterCondition;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.utils.Configurator;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.mongodb.MapReduceCommand.OutputType.INLINE;

/**
 * Created by artur on 17/08/16.
 */
public class SelectiveFeatureDistributionSampling extends RepresentativeGenerator {

    /**
     * Default logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SelectiveFeatureDistributionSampling.class);
    Map<List<String>, Integer> tuples;
    long start, stop;

    //Map<String, Object> samplingOptions;
    double cumulativePropertyCoverageInCollection, cumulativeNumberOfDistinctTuples, TargetNumberOfDistinctTuplesForSamples;
    int sample_size, numberOfDistinctTuplesinCollection;
    long numberOfFilesInCollection;
    List<Element> sampleElements = new ArrayList<Element>();
    List<String> properties;
    double targetPropertyCoverage;
    double targetTupleCoverage;
    String proportion;
    int fileThreshold;
    String location;
    /**
     * The persistence layer.
     */
    private MongoPersistenceLayer pl;
    private Map<String, List<Integer>> bins;

    public SelectiveFeatureDistributionSampling() {
        this.pl = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();
        options = new HashMap<String, Object>();
    }

    public static void addToZipFile(String fileName, ZipOutputStream zos) throws FileNotFoundException, IOException {

        LOG.debug("Writing '" + fileName + "' to zip file");

        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public List<String> execute() {
        start = System.currentTimeMillis();
        List<String> result = new ArrayList<String>();
        List<BasicDBObject> results = runMapReduce();
        tuples = readResults(results);
        numberOfFilesInCollection = pl.count(Element.class, filter);
        numberOfDistinctTuplesinCollection = tuples.size();
        TargetNumberOfDistinctTuplesForSamples = numberOfDistinctTuplesinCollection * targetTupleCoverage;
        cumulativePropertyCoverageInCollection = 0;
        cumulativeNumberOfDistinctTuples = 0;
        int tmp_threshold = 0;
        Iterator<Map.Entry<List<String>, Integer>> tuplesIterator = tuples.entrySet().iterator();
        while ((cumulativePropertyCoverageInCollection < targetPropertyCoverage && cumulativeNumberOfDistinctTuples < TargetNumberOfDistinctTuplesForSamples && tmp_threshold < fileThreshold) && tuplesIterator.hasNext()) {
            Map.Entry<List<String>, Integer> tuple = tuplesIterator.next();
            int numberOfFilesWithTuple = tuple.getValue();
            double tmp_populationCoverageOfTupleInColleciton = numberOfFilesWithTuple / (double) numberOfFilesInCollection;
            int samplesPerTuple = samplesPerTuple(tmp_populationCoverageOfTupleInColleciton);
            Iterator<Element> samplesForValues = getSamplesForValues(tuple.getKey());
            List<String> samples = pickSamples(samplesForValues, samplesPerTuple);
            tmp_threshold += samples.size();
            cumulativeNumberOfDistinctTuples += 1;
            if (samples.size() > 0) {
                cumulativePropertyCoverageInCollection += tmp_populationCoverageOfTupleInColleciton;
            }
            result.addAll(samples);
        }
        sample_size = result.size();
        stop = System.currentTimeMillis();
        exportResults(location);

        return result;
    }

    private String exportResults(String location) {
        String writeSamplesToCSV = writeSamplesToCSV();
        String writeResultsToXML = writeResultsToXML();
        String writeTuplesToCSV = writeTuplesToCSV();
        String writePTTables = writePTTables();
        String outputFileLocation = System.getProperty("java.io.tmpdir") + "/sfd_results.zip";
        if (location != null)
            outputFileLocation = location;
        try {
            File f = new File(outputFileLocation);
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(outputFileLocation);
            ZipOutputStream zos = new ZipOutputStream(fos);
            addToZipFile(writeSamplesToCSV, zos);
            addToZipFile(writeResultsToXML, zos);
            addToZipFile(writePTTables, zos);
            addToZipFile(writeTuplesToCSV, zos);
            zos.close();
            fos.close();

            LOG.info("Writing a zip-file with results to {}", outputFileLocation);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFileLocation;
    }

    private String writeTuplesToCSV() {
        String outputFileLocation = System.getProperty("java.io.tmpdir") + "/tuples.csv";

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(outputFileLocation), '\t');
            String[] header = new String[properties.size() + 1];
            int i = 0;
            for (String property : properties) {
                header[i] = property;
                i++;
            }
            header[header.length - 1] = "count";
            writer.writeNext(header);
            for (Map.Entry<List<String>, Integer> listIntegerEntry : tuples.entrySet()) {
                List<String> key = listIntegerEntry.getKey();
                Integer value = listIntegerEntry.getValue();
                String[] data = new String[properties.size() + 1];
                int j = 0;
                for (String property : key) {
                    data[j] = property;
                    j++;
                }
                data[data.length - 1] = value.toString();
                writer.writeNext(data);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFileLocation;
    }

    private String writePTTables() {
        String result = "sample_size, targetPropertyCoverage, targetTupleCoverage,";
        double pcovTC = 0;
        double Tsp = 0;
        int sample_size = 0;
        for (Map.Entry<List<String>, Integer> listIntegerEntry : tuples.entrySet()) {
            List<String> key = listIntegerEntry.getKey();
            Integer value = listIntegerEntry.getValue();
            double tmp_pcovTC = value / (double) numberOfFilesInCollection;
            pcovTC += tmp_pcovTC;
            double tmp_Tsp1 = 1 / (double) tuples.size();
            Tsp += tmp_Tsp1;
            int samplesPerTuple = samplesPerTuple(tmp_pcovTC);
            sample_size += samplesPerTuple;
            result += "\n " + sample_size + ", " + Double.toString(new BigDecimal(pcovTC).setScale(3, RoundingMode.HALF_UP).doubleValue()) + ", " + Double.toString(new BigDecimal(Tsp).setScale(3, RoundingMode.HALF_UP).doubleValue()) + ",";
        }


        String outputFileLocation = System.getProperty("java.io.tmpdir") + "/PTtable.csv";
        File f = new File(outputFileLocation);
        try {
            f.createNewFile();
            final FileWriter writer = new FileWriter(f);

            writer.write(result);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFileLocation;

    }

    private String writeResultsToXML() {
        /*
        One txt with
Input params used
Resulting targetPropertyCoverage of the sample
Resulting targetTupleCoverage of the sample
Sample size
Timestamp
Runtime of algorithm
Source collection and filter
Anything else that is interesting about inputs, outputs,settings,params
        */
        long time = (stop - start) / 1000;
        final Document document = DocumentHelper.createDocument();
        org.dom4j.Element sfd_results = document.addElement("sfd_results");
        org.dom4j.Element input = sfd_results.addElement("input");

        org.dom4j.Element pcoverage = input.addElement("targetPropertyCoverage");
        pcoverage.addText(Double.toString(this.targetPropertyCoverage));
        org.dom4j.Element tcoverage = input.addElement("targetTupleCoverage");
        tcoverage.addText(Double.toString(this.targetTupleCoverage));
        org.dom4j.Element threshold = input.addElement("fileThreshold");
        threshold.addText(Double.toString(this.fileThreshold));
        org.dom4j.Element proportion = input.addElement("proportion");
        proportion.addText(this.proportion);
        org.dom4j.Element props = input.addElement("properties");
        for (String property : properties) {
            org.dom4j.Element property1 = props.addElement("property");
            property1.addText(property);
        }
        org.dom4j.Element output = sfd_results.addElement("output");

        java.util.Date date = new java.util.Date();
        Timestamp timestamp = new Timestamp(date.getTime());

        org.dom4j.Element timestamp1 = output.addElement("timestamp");
        timestamp1.addText(timestamp.toString());
        org.dom4j.Element processing_time = output.addElement("processing_time");
        processing_time.addText(Long.toString(time));
        processing_time.addAttribute("unit", "seconds");

        org.dom4j.Element filter = output.addElement("filter");
        if (this.filter != null)
            filter.addText(this.filter.toString());
        else
            filter.addText("empty");
        org.dom4j.Element tcoverage1 = output.addElement("targetTupleCoverage");
        tcoverage1.addText(Double.toString(cumulativeNumberOfDistinctTuples / TargetNumberOfDistinctTuplesForSamples));
        org.dom4j.Element pcoverage1 = output.addElement("targetPropertyCoverage");
        pcoverage1.addText(Double.toString(cumulativePropertyCoverageInCollection));
        org.dom4j.Element sample_size = output.addElement("sample_size");
        sample_size.addText(Integer.toString(this.sample_size));

        document.toString();


        String outputFileLocation = System.getProperty("java.io.tmpdir") + "/results.xml";
        File f = new File(outputFileLocation);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            final OutputFormat format = OutputFormat.createPrettyPrint();
            final XMLWriter writer = new XMLWriter(new FileWriter(outputFileLocation), format);
            writer.write(document);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFileLocation;
    }

    private String writeSamplesToCSV() {
        CSVGenerator csvGenerator = new CSVGenerator(pl);

        final Iterator<Property> allprops = pl.find(Property.class, null);
        final List<Property> props = csvGenerator.getProperties(allprops);
        String outputFileLocation = System.getProperty("java.io.tmpdir") + "/samples.csv";
        File f = new File(outputFileLocation);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        csvGenerator.write(sampleElements.iterator(), props, outputFileLocation);
        return outputFileLocation;
    }

    List<String> pickSamples(Iterator<Element> iterator, int count) {
        List<String> result = new ArrayList<String>();
        int i = 0;
        while (iterator.hasNext() && i < count) {
            Element next = iterator.next();
            sampleElements.add(next);
            String uid = next.getUid();
            result.add(uid);
            i++;
        }
        return result;
    }

    private int samplesPerTuple(double tmp_pcovTC) {
        if (fileThreshold <= 0) {
            return 1;
        }
        if (proportion.equals("linear")) {
            int i = ((Double) (fileThreshold * tmp_pcovTC)).intValue();
            if (i == 0)
                return 1;
            return ((Double) (fileThreshold * tmp_pcovTC)).intValue();
        } else if (proportion.equals("logarithmic")) {
            int i = ((Double) (fileThreshold * Math.log(tmp_pcovTC))).intValue();
            if (i == 0)
                return 1;
            return ((Double) (fileThreshold * Math.log(tmp_pcovTC))).intValue();

        } else if (proportion.equals("no")) {
            return ((Double) (fileThreshold * 1.0 / numberOfDistinctTuplesinCollection)).intValue();
        }
        return 1;
    }

    int tupleCount(List<String> tuple) {
        for (Map.Entry<List<String>, Integer> listIntegerEntry : tuples.entrySet()) {
            if (listIntegerEntry.getKey().equals(tuple)) {
                return listIntegerEntry.getValue();
            }
        }
        return 0;
    }

    public Iterator<Element> getSamplesForValues(List<String> values) {
        Filter f = new Filter(this.filter);
        for (int i = 0; i < properties.size(); i++) {
            String val = values.get(i);
            String prop = properties.get(i);

            //check if the existing filter contains this property
            //if yes, then skip the property

            boolean skip = false;
            for (PropertyFilterCondition propertyFilterCondition : f.getPropertyFilterConditions()) {
                if (propertyFilterCondition.getProperty().equals(prop))
                    skip = true;
            }
            if (skip)
                continue;

            Property property = pl.getCache().getProperty(prop);
            if (property.getType().equals(PropertyType.INTEGER.name()) && val.contains("-") && !val.startsWith("-")) {
                PropertyFilterCondition pfc = new PropertyFilterCondition();
                pfc.setProperty(prop);
                if (val.equals("CONFLICT")) {
                    pfc.addCondition(PropertyFilterCondition.PropertyFilterConditionType.STATUS, MetadataRecord.Status.CONFLICT);
                } else
                    pfc.addCondition(PropertyFilterCondition.PropertyFilterConditionType.VALUE, val);
                //BetweenFilterCondition betweenFilterCondition = BetweenFilterCondition.getBetweenFilterCondition(val, prop);
                pfc.setStrict(false);
                f.getPropertyFilterConditions().add(pfc);//addFilterCondition(betweenFilterCondition);
            }
            if (property.getType().equals(PropertyType.BOOL.name())) {
                PropertyFilterCondition pfc = new PropertyFilterCondition();
                pfc.setProperty(prop);
                if (val.equals("CONFLICT")) {
                    pfc.addCondition(PropertyFilterCondition.PropertyFilterConditionType.STATUS, MetadataRecord.Status.CONFLICT);
                } else
                    pfc.addCondition(PropertyFilterCondition.PropertyFilterConditionType.VALUE, val);
                pfc.setStrict(false);
                f.getPropertyFilterConditions().add(pfc);
                //f.addFilterCondition(new FilterCondition(properties.get(i), Boolean.parseBoolean(values.get(i))));
            } else {
                PropertyFilterCondition pfc = new PropertyFilterCondition();
                pfc.setProperty(prop);
                if (val.equals("CONFLICT")) {
                    pfc.addCondition(PropertyFilterCondition.PropertyFilterConditionType.STATUS, MetadataRecord.Status.CONFLICT);
                } else
                    pfc.addCondition(PropertyFilterCondition.PropertyFilterConditionType.VALUE, val);
                pfc.setStrict(false);
                f.getPropertyFilterConditions().add(pfc);
                //f.addFilterCondition(new FilterCondition(properties.get(i), values.get(i)));
            }
        }

        long count = pl.count(Element.class, f);
        return pl.find(Element.class, f);
    }

    @Override
    public void setOptions(Map<String, Object> options) {
        this.options = options;
        readOptions();
    }

    private Map<List<String>, Integer> readResults(List<BasicDBObject> results) {
        Map<List<String>, Integer> tmp = new HashMap<List<String>, Integer>();
        for (BasicDBObject result : results) {
            int value = result.getInt("value");
            //  BasicDBList basicDBList = (BasicDBList) result.get("_id");
            BasicDBObject id = (BasicDBObject) result.get("_id");
            Collection<Object> values = id.values();
            List<String> strings = new ArrayList<>();
            for (Object o : values) {
                strings.add(o.toString());
            }
            tmp.put(strings, value);
        }

        Map<List<String>, Integer> listIntegerMap = sortByValue(tmp);
        return listIntegerMap;

    }

    private List<BasicDBObject> runMapReduce() {
        String map = "function() {\n" +
                "    var properties = @1;\n" +
                "    var bins= @2;\n" +
                "    var toEmit={};\n" +
                "    for (var x in properties) {\n" +
                "        var found=false;\n" +
                "        var property = properties[x];\n" +
                "        for (var mr in this.metadata ){\n" +
                "            var metadataRecord=this.metadata[mr];\n" +
                "            if (metadataRecord.property==property){\n" +
                "                found=true;\n" +
                "                if (metadataRecord.status != 'CONFLICT'){\n" +
                "                    if (metadataRecord.property=='created'){\n" +
                "                        var date = metadataRecord.sourcedValues[0].value;\n" +
                "                        toEmit[property]=(date.getFullYear().toString());  \n" +
                "                    }\n" +
                "                    else{\n" +
                "                        var val = metadataRecord.sourcedValues[0].value;\n" +
                "                        if (bins[property]!=null){\n" +
                "                            var skipped=false;\n" +
                "                            for (t in bins[property]){\n" +
                "                                fileThreshold = bins[property][t];  \n" +
                "                                if (val>=fileThreshold[0] && val<=fileThreshold[1]){\n" +
                "                                    toEmit[property]=(fileThreshold[0]+'-'+fileThreshold[1]);\n" +
                "                                    skipped=true;\n" +
                "                                    break;\n" +
                "                                }   \n" +
                "                            }\n" +
                "                            if (!skipped)\n" +
                "                                toEmit[property]=(val); \n" +
                "                        }\n" +
                "                        else\n" +
                "                            toEmit[property]=(val); \n" +
                "                    } \n" +
                "                }\n" +
                "                else \n" +
                "                    toEmit[property]=(\"CONFLICT\"); \n" +
                "                \n" +
                "            }\n" +
                "        }\n" +
                "        if (!found)\n" +
                "            toEmit[property]=(\"Unknown\"); \n" +
                "    }\n" +
                "    emit(toEmit, 1);\n" +
                "}";

        String reduce = "function reduce(key, values) {\n" +
                "        var res = 0;\n" +
                "        values.forEach(function(v) {\n" +
                "            res += v;\n" +
                "        });\n" +
                "        return res;\n" +
                "    }";
        DBObject query = pl.getCachedFilter(filter);
        map = map.replace("@1", ListToString(properties));
        map = map.replace("@2", binsToJSON());
        LOG.debug("filter query is:\n{}", query);
        DBCollection elmnts = pl.getCollection(Element.class);
        MapReduceCommand cmd = new MapReduceCommand(elmnts, map, reduce, null, INLINE, query);
        MapReduceOutput output = elmnts.mapReduce(cmd);
        // List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get( "results" );

        Iterator<DBObject> iterator = output.results().iterator();
        List<BasicDBObject> results = new ArrayList<BasicDBObject>();
        while (iterator.hasNext()) {
            results.add((BasicDBObject) iterator.next());

        }
        return results;
    }

    @Override
    public List<String> execute(int limit) {
        return execute();
    }

    public void readOptions() {
        if (options.get("properties") != null)
            properties = (List<String>) options.get("properties");
        if (options.get("targetPropertyCoverage") != null)
            targetPropertyCoverage = (double) Double.parseDouble((String) options.get("targetPropertyCoverage"));
        if (options.get("targetTupleCoverage") != null)
            targetTupleCoverage = (double) Double.parseDouble((String) options.get("targetTupleCoverage"));
        if (options.get("fileThreshold") != null)
            fileThreshold = (int) Integer.parseInt((String) options.get("fileThreshold"));
        if (options.get("proportion") != null)
            proportion = (String) options.get("proportion");
        if (options.get("location") != null)
            location = (String) options.get("location");
        if (options.get("bins") != null)
            bins = (Map<String, List<Integer>>) options.get("bins");
    }

    @Override
    public String getType() {
        return null;
    }

    private String binsToJSON() {
        String result = "{";
        for (Map.Entry<String, List<Integer>> entry : bins.entrySet()) {
            String key = entry.getKey();
            List<Integer> value = entry.getValue();
            String binThresholds = getBinThresholds(value);
            result += "'" + key + "':" + binThresholds + ",";
        }
        if (bins.size() > 0)
            result = result.substring(0, result.length() - 1);
        result += "}";

        return result;
    }

    private String getBinThresholds(List<Integer> binThresholds) {
        if (binThresholds == null)
            return "[]";
        String result = "[";
        for (int i = 0; i < binThresholds.size(); i++) {
            String threshold = "[";
            if (i == 0) {
                threshold += "0," + (binThresholds.get(i) - 1);
            } else {
                threshold += binThresholds.get(i - 1) + "," + (binThresholds.get(i) - 1);
            }
            threshold += "]";
            result += threshold + ",";
        }
        if (binThresholds.size() > 0)
            result = result.substring(0, result.length() - 1);
        result += "]";
        return result;
    }

    private String ListToString(List<String> properties) {
        String propertiesString = "[";
        for (String p : properties) {
            propertiesString += "'" + p + "',";
        }
        if (properties.size() > 0)
            propertiesString = propertiesString.substring(0, propertiesString.length() - 1);
        propertiesString += "]";
        return propertiesString;
    }


}
