package com.petpet.c3po.analysis;

import com.mongodb.*;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.utils.Configurator;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

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
    private static final Logger LOG = LoggerFactory.getLogger(SystematicSamplingRepresentativeGenerator.class);

    //Map<String, Object> samplingOptions;

    public SelectiveFeatureDistributionSampling(){
        this.pl = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();
        options=new HashMap<String, Object>();
    }
    Map<List<String>, Integer> tuples;
    long start, stop;
    double pcovSC, tmp_Tsp;
    int sample_size;
    @Override
    public List<String> execute() {
        start = System.currentTimeMillis();
        List<String> result=new ArrayList<String>();
        List<BasicDBObject>  results = runMapReduce();
        tuples = readResults(results);
        long popC = pl.count(Element.class, filter);
        int Tcp=tuples.size();
        double Tsp = Tcp * tcoverage;
        pcovSC=0;
        tmp_Tsp=0;
        int tmp_threshold=0;
        Iterator<Map.Entry<List<String>, Integer>> tuplesIterator = tuples.entrySet().iterator();
        while ((pcovSC<=pcoverage && tmp_Tsp<=Tsp && tmp_threshold<=threshold) && tuplesIterator.hasNext()){
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
        exportResults();

        return result;
    }

    private void exportResults() {
        writeSamplesToCSV();
        writeResultsToXML();
    }

    private void writeResultsToXML() {
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

        org.dom4j.Element input=document.addElement("input");

        input.addElement("pcoverage", Double.toString(pcoverage));
        input.addElement("tcoverage", Double.toString(tcoverage));
        input.addElement("threshold", Double.toString(threshold));
        input.addElement("proportion", proportion);
        org.dom4j.Element props = input.addElement("properties");
        for (String property : properties) {
            props.addElement("property", property);
        }

        java.util.Date date= new java.util.Date();
        Timestamp timestamp = new Timestamp(date.getTime());

        input.addElement("timestamp", timestamp.toString());
        org.dom4j.Element processing_time = input.addElement("processing_time", Long.toString(time));
        processing_time.addAttribute("unit", "seconds");

        org.dom4j.Element output = document.addElement("output");

        output.addElement("filter", filter.toString());
        output.addElement("tcoverage",  Double.toString(tmp_Tsp));
        output.addElement("pcoverage", Double.toString(pcovSC) );
        output.addElement("sample_size", Integer.toString(sample_size) );

        document.toString();
    }

    private void writeSamplesToCSV() {
        CSVGenerator csvGenerator=new CSVGenerator(pl);

        final Iterator<Property> allprops = pl.find( Property.class, null );
        final List<Property> props = csvGenerator.getProperties( allprops );
        String output="";
        csvGenerator.write(sampleElements.iterator(),props,output);


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
            return 1;
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
        Filter f=new Filter();
        for (int i = 0; i < properties.size(); i++) {
            f.addFilterCondition(new FilterCondition(properties.get(i), values.get(i)));
        }
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
                strings.add((String) o);
            }
            tmp.put(strings,value);
        }

        Map<List<String>, Integer> listIntegerMap = sortByValue(tmp);
        return listIntegerMap;

    }

    private List<BasicDBObject> runMapReduce() {
        String map="function() {\n" +
                "    var properties = @1;\n" +
                "    var toEmit=[];\n" +
                "    for (x in properties) {\n" +
                "        property = properties[x];\n" +
                "        if (this[property] != null) {\n" +
                "            if (this[property].status != 'CONFLICT') {\n" +
                "               if (property=='created') {" +
                "                    var date = new Date(this[property].values[0]);" +
                "                    toEmit.push(date.getFullYear().toString());" +
                "               } " +
            "                   else    " +
                "                   toEmit.push(this[property].values[0]); \n" +
                "            } \n" +
                "            else {\n" +
                "                toEmit.push(\"CONFLICT\"); \n" +
                "            }\n" +
                "        } \n" +
                "        else {\n" +
                "            toEmit.push(\"Unknown\");\n" +
                "        }\n" +
                "    }\n" +
                "    emit(toEmit, 1);\n" +
                "}";
        String reduce= "function reduce(key, values) {\n" +
                "        var res = 0;\n" +
                "        values.forEach(function(v) {\n" +
                "            res += v;\n" +
                "        });\n" +
                "        return res;\n" +
                "    }";
        DBObject query = pl.getCachedFilter( filter );
        map = map.replace("@1", ListToString(properties));
        LOG.debug( "filter query is:\n{}", query );
        DBCollection elmnts = pl.getCollection( Element.class );
        MapReduceCommand cmd = new MapReduceCommand( elmnts, map, reduce, null, INLINE, query );
        MapReduceOutput output = elmnts.mapReduce( cmd );
        List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get( "results" );
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
    }

    @Override
    public String getType() {
        return null;
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
