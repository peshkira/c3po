package com.petpet.c3po.analysis;

import com.mongodb.*;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.BetweenFilterCondition;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.utils.Configurator;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.mongodb.MapReduceCommand.OutputType.INLINE;

/**
 * Created by artur on 17/08/16.
 */
public class SelectiveFeatureDistributionSampling extends RepresentativeGenerator {

    /**
     * The persistence layer.
     */
    private MongoPersistenceLayer pl;

    /**
     * Default logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SelectiveFeatureDistributionSampling.class);
    private Map<String, List<Integer>> bins;

    //Map<String, Object> samplingOptions;

    public SelectiveFeatureDistributionSampling(){
        this.pl = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();
        options=new HashMap<String, Object>();
    }
    Map<List<String>, Integer> tuples;
    long start, stop;
    double pcovSC, tmp_Tsp, Tsp;
    int sample_size, Tcp;
    long popC;

    @Override
    public List<String> execute() {
        start = System.currentTimeMillis();
        List<String> result=new ArrayList<String>();
        List<BasicDBObject>  results = runMapReduce();
        tuples = readResults(results);
        popC = pl.count(Element.class, filter);
        Tcp=tuples.size();
        Tsp = Tcp * tcoverage;
        pcovSC=0;
        tmp_Tsp=0;
        int tmp_threshold=0;
        Iterator<Map.Entry<List<String>, Integer>> tuplesIterator = tuples.entrySet().iterator();
        while ((pcovSC<pcoverage && tmp_Tsp<Tsp && tmp_threshold<threshold) && tuplesIterator.hasNext()){
            Map.Entry<List<String>, Integer> next = tuplesIterator.next();
            int popT = next.getValue();
            double tmp_pcovTC = popT / (double) popC;
            int samplesPerTuple = samplesPerTuple(tmp_pcovTC);
            Iterator<Element> samplesForValues = getSamplesForValues(next.getKey());
            List<String> samples = pickSamples(samplesForValues, samplesPerTuple);
            tmp_threshold+=samples.size();
            tmp_Tsp += 1;
            if (samples.size()>0) {
                pcovSC += tmp_pcovTC;
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
        String writePTTables = writePTTables();
        String outputFileLocation=System.getProperty("java.io.tmpdir") + "/sfd_results.zip";
        if (location!=null)
            outputFileLocation=location;
        try {
            File f =new File(outputFileLocation);
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(outputFileLocation);
            ZipOutputStream zos = new ZipOutputStream(fos);
            addToZipFile(writeSamplesToCSV, zos);
            addToZipFile(writeResultsToXML, zos);
            addToZipFile(writePTTables, zos);
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

    private String writePTTables() {
        String result="sample_size, pcoverage, tcoverage,";
        double pcovTC=0;
        double Tsp=0;
        int sample_size=0;
        for (Map.Entry<List<String>, Integer> listIntegerEntry : tuples.entrySet()) {
            List<String> key = listIntegerEntry.getKey();
            Integer value = listIntegerEntry.getValue();
            double tmp_pcovTC = value / (double) popC;
            pcovTC+=tmp_pcovTC;
            double tmp_Tsp1=1/(double) tuples.size();
            Tsp+=tmp_Tsp1;
            int samplesPerTuple = samplesPerTuple(tmp_pcovTC);
            sample_size+=samplesPerTuple;
            result += "\n " + sample_size + ", " + Double.toString( new BigDecimal(pcovTC).setScale(3, RoundingMode.HALF_UP).doubleValue()) +", " + Double.toString(new BigDecimal(Tsp).setScale(3, RoundingMode.HALF_UP).doubleValue()) + ",";
        }


        String outputFileLocation=System.getProperty("java.io.tmpdir") + "/PTtable.csv";
        File f=new File(outputFileLocation);
        try {
            f.createNewFile();
            final FileWriter writer = new FileWriter( f );

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
Resulting pcoverage of the sample
Resulting tcoverage of the sample
Sample size
Timestamp
Runtime of algorithm
Source collection and filter
Anything else that is interesting about inputs, outputs,settings,params
        */
        long time=(stop-start)/1000;
        final Document document = DocumentHelper.createDocument();
        org.dom4j.Element sfd_results = document.addElement("sfd_results");
        org.dom4j.Element input=sfd_results.addElement("input");

        org.dom4j.Element pcoverage = input.addElement("pcoverage" );
        pcoverage.addText(Double.toString(this.pcoverage));
        org.dom4j.Element tcoverage = input.addElement("tcoverage");
        tcoverage.addText(Double.toString(this.tcoverage));
        org.dom4j.Element threshold = input.addElement("threshold");
        threshold.addText( Double.toString(this.threshold));
        org.dom4j.Element proportion = input.addElement("proportion");
        proportion.addText(this.proportion);
        org.dom4j.Element props = input.addElement("properties");
        for (String property : properties) {
            org.dom4j.Element property1 = props.addElement("property");
            property1.addText( property);
        }
        org.dom4j.Element output = sfd_results.addElement("output");

        java.util.Date date= new java.util.Date();
        Timestamp timestamp = new Timestamp(date.getTime());

        org.dom4j.Element timestamp1 = output.addElement("timestamp");
        timestamp1.addText( timestamp.toString());
        org.dom4j.Element processing_time = output.addElement("processing_time");
        processing_time.addText( Long.toString(time));
        processing_time.addAttribute("unit", "seconds");

        org.dom4j.Element filter = output.addElement("filter");
        if (this.filter !=null)
            filter.addText( this.filter.toString());
        else
            filter.addText(  "empty");
        org.dom4j.Element tcoverage1 = output.addElement("tcoverage");
        tcoverage1.addText( Double.toString(tmp_Tsp/Tsp));
        org.dom4j.Element pcoverage1 = output.addElement("pcoverage");
        pcoverage1.addText( Double.toString(pcovSC));
        org.dom4j.Element sample_size = output.addElement("sample_size");
        sample_size.addText(Integer.toString(this.sample_size));

        document.toString();


        String outputFileLocation=System.getProperty("java.io.tmpdir") + "/results.xml";
        File f=new File(outputFileLocation);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            final OutputFormat format = OutputFormat.createPrettyPrint();
            final XMLWriter writer = new XMLWriter( new FileWriter( outputFileLocation ), format );
            writer.write( document );
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFileLocation;
    }

    private String writeSamplesToCSV() {
        CSVGenerator csvGenerator=new CSVGenerator(pl);

        final Iterator<Property> allprops = pl.find( Property.class, null );
        final List<Property> props = csvGenerator.getProperties( allprops );
        String outputFileLocation=System.getProperty("java.io.tmpdir") + "/samples.csv";
        File f=new File(outputFileLocation);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        csvGenerator.write(sampleElements.iterator(),props,outputFileLocation);
        return outputFileLocation;
    }

    List<Element> sampleElements=new ArrayList<Element>();

    List<String> pickSamples(Iterator<Element> iterator, int count){
        List<String> result=new ArrayList<String>();
        int i=0;
        while(iterator.hasNext() && i < count){
            Element next = iterator.next();
            sampleElements.add(next);
            String uid = next.getUid();
            result.add(uid);
            i++;
        }
        return result;
    }

    private int samplesPerTuple(double tmp_pcovTC) {
        if (threshold<=0){
            return 1;
        }
        if (proportion.equals("linear")){
            int i = ((Double) (threshold * tmp_pcovTC)).intValue();
            if (i==0)
                return 1;
            return ((Double)(threshold*tmp_pcovTC)).intValue();
        } else if (proportion.equals("logarithmic"))
        {
            int i =((Double)(threshold*Math.log(tmp_pcovTC))).intValue();
            if (i==0)
                return 1;
            return ((Double)(threshold*Math.log(tmp_pcovTC))).intValue();

        } else if (proportion.equals("no")){
            return ((Double) (threshold*1.0/Tcp)).intValue();
        }
        return 1;
    }

    int tupleCount(List<String> tuple){
        for (Map.Entry<List<String>, Integer> listIntegerEntry : tuples.entrySet()) {
            if (listIntegerEntry.getKey().equals(tuple)){
                return listIntegerEntry.getValue();
            }
        }
        return 0;
    }



    public Iterator<Element> getSamplesForValues(List<String> values){
        Filter f=new Filter(this.filter);
        for (int i = 0; i < properties.size(); i++) {
            String val = values.get(i);
            String prop = properties.get(i);
            Property property = pl.getCache().getProperty(prop);
            if (property.getType().equals(PropertyType.INTEGER.name()) && val.contains("-") && !val.startsWith("-")){
                BetweenFilterCondition betweenFilterCondition = BetweenFilterCondition.getBetweenFilterCondition(val, prop);
                f.addFilterCondition(betweenFilterCondition);
            }
            if (property.getType().equals(PropertyType.BOOL.name())){
                f.addFilterCondition(new FilterCondition(properties.get(i), Boolean.parseBoolean(values.get(i))));
            }
            else
                f.addFilterCondition(new FilterCondition(properties.get(i), values.get(i)));
        }

        long count = pl.count(Element.class, f);
        return pl.find(Element.class,f);
    }

    @Override
    public void setOptions( Map<String, Object> options ) {
        this.options = options;
        readOptions();
    }



    private Map<List<String>, Integer>  readResults(List<BasicDBObject> results) {
        Map<List<String>, Integer> tmp=new HashMap<List<String>, Integer>();
        for (BasicDBObject result : results) {
            int value = result.getInt("value");
            BasicDBList basicDBList = (BasicDBList) result.get("_id");
            List<String> strings=new ArrayList<>();
            for (Object o : basicDBList) {
                strings.add( o.toString());
            }
            tmp.put(strings,value);
        }

        Map<List<String>, Integer> listIntegerMap = sortByValue(tmp);
        return listIntegerMap;

    }

    private List<BasicDBObject> runMapReduce() {
        String map="function() {\n" +
                "    var properties = @1;\n" +
                "    var bins= @2;\n" +
                "var toEmit=[];\n" +
                "    for (x in properties) {\n" +
                "        property = properties[x];\n" +
                "        if (this[property] != null) {\n" +
                "            if (this[property].status != 'CONFLICT') {\n" +
                "                if (property=='created') {                    \n" +
                "                    var date = new Date(this[property].values[0]);                    \n" +
                "                    toEmit.push(date.getFullYear().toString());             \n" +
                "                }\n" +
                "                else{\n" +
                "                    var val=this[property].values[0];\n" +
                "                    if (bins[property]!=null){\n" +
                "                        var skipped=false;\n" +
                "                        for (t in bins[property]){\n" +
                "                            threshold = bins[property][t];  \n" +
                "                            if (val>=threshold[0] && val<=threshold[1]){\n" +
                "                                toEmit.push(threshold[0]+'-'+threshold[1]);\n" +
                "                                skipped=true;\n" +
                "                                break;\n" +
                "                            }   \n" +
                "                        }\n" +
                "                        if (!skipped)\n" +
                "                            toEmit.push(val); \n" +
                "                    }\n" +
                "                    else\n" +
                "                        toEmit.push(val); \n" +
                "                } \n" +
                "            }\n" +
                "            else {\n" +
                "                toEmit.push(\"CONFLICT\"); \n" +
                "            }\n" +
                "        } \n" +
                "        else {\n" +
                "            toEmit.push(\"Unknown\");\n" +
                "        }\n" +
                "    }\n" +
                "    emit(toEmit, 1);" +
                "}\n";

        String reduce= "function reduce(key, values) {\n" +
                "        var res = 0;\n" +
                "        values.forEach(function(v) {\n" +
                "            res += v;\n" +
                "        });\n" +
                "        return res;\n" +
                "    }";
        DBObject query = pl.getCachedFilter( filter );
        map = map.replace("@1", ListToString(properties));
        map=map.replace("@2", binsToJSON());
        LOG.debug( "filter query is:\n{}", query );
        DBCollection elmnts = pl.getCollection( Element.class );
        MapReduceCommand cmd = new MapReduceCommand( elmnts, map, reduce, null, INLINE, query );
        MapReduceOutput output = elmnts.mapReduce( cmd );
       // List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get( "results" );

        Iterator<DBObject> iterator = output.results().iterator();
        List<BasicDBObject> results = new ArrayList<BasicDBObject> ();
        while (iterator.hasNext()){
            results.add( (BasicDBObject) iterator.next());

        }
        return results;
    }

    @Override
    public List<String> execute(int limit) {
        return execute();
    }
    List<String> properties;
    double pcoverage;
    double tcoverage;
    String proportion;
    int threshold;
    String location;
    public void readOptions(){
        if ( options.get("properties") !=null)
            properties = (List<String>) options.get("properties");
        if ( options.get("pcoverage") !=null)
            pcoverage=(double) Double.parseDouble( (String) options.get("pcoverage"));
        if ( options.get("tcoverage") !=null)
            tcoverage=(double) Double.parseDouble( (String) options.get("tcoverage"));
        if ( options.get("threshold") !=null)
            threshold=(int) Integer.parseInt( (String) options.get("threshold"));
        if ( options.get("proportion") !=null)
            proportion=(String) options.get("proportion");
        if ( options.get("location") !=null)
            location=(String) options.get("location");
        if ( options.get("bins") !=null)
            bins=(Map<String, List<Integer>>) options.get("bins");
    }

    @Override
    public String getType() {
        return null;
    }

    private String binsToJSON(){
        String result="{";
        for (Map.Entry<String, List<Integer>> entry : bins.entrySet()) {
            String key = entry.getKey();
            List<Integer> value = entry.getValue();
            String binThresholds = getBinThresholds(value);
            result+="'"+key+"':"+binThresholds+",";
        }
        if (bins.size()>0)
            result = result.substring(0, result.length() - 1);
        result += "}";

        return result;
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


    public static <K, V extends Comparable<? super V>> Map<K, V>  sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );
        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }



}
