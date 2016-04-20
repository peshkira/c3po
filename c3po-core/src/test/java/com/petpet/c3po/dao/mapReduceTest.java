package com.petpet.c3po.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.petpet.c3po.api.model.Source;
import com.sun.javafx.binding.IntegerConstant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.utils.DataHelper;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

/**
 * Created by artur on 13/04/16.
 */
public class mapReduceTest {
    private static final Logger LOG = LoggerFactory.getLogger(MongoPersistenceLayerTest.class);

    MongoPersistenceLayer pLayer;

    @Before
    public void setup() {
        pLayer = new MongoPersistenceLayer();

        Map<String, String> config = new HashMap<String, String>();
        config.put("db.host", "localhost");
        config.put("db.port", "27017");
        config.put("db.name", "c3po");

        DataHelper.init();

        try {
            pLayer.establishConnection(config);
        } catch (C3POPersistenceException e) {
            LOG.warn("Could not establish a connection to the persistence layer. All tests will be skipped");
        }
    }

    @Test
    public void mapReduceGenericTest() throws Exception {

        String map2 = "function map() {\n" +
                "\t\tvar result={};\n" +
                "\t\tif ( this['format'] != null && this['format'].status != null && \n" +
                "\t\t\tthis['mimetype'] !=null && this['mimetype'].status != null && \n" +
                "\t\t\tthis['format_version'] !=null && this['format_version'].status != null &&\n" +
                "\t\t\t(this['format'].status == 'CONFLICT' || this['format_version'].status == 'CONFLICT' || this['mimetype'].status == 'CONFLICT')) {\n" +
                "\t\t\t\n" +
                "\t\t\t\tvar format={};\n" +
                "\t\t\t\tformat.status=this['format'].status;\n" +
                "\t\t\t\tif (this['format'].status == 'CONFLICT'){\n" +
                "\t\t\t\t\tformat.values=this['format'].values;\n" +
                "\t\t\t\t} else{\n" +
                "\t\t\t\t\tformat.values=[this['format'].value];\n" +
                "\t\t\t\t}\n" +
                "\t\t\t\tformat.sources=this['format'].sources;\n" +
                "\t\t\t\tresult.format=format;\n" +
                "\n" +
                "\t\t\t\tvar format_version={};\n" +
                "\t\t\t\tformat_version.status=this['format_version'].status;\n" +
                "\t\t\t\tif (this['format_version'].status == 'CONFLICT'){\n" +
                "\t\t\t\t\tformat_version.values=this['format_version'].values;\n" +
                "\t\t\t\t} else{\n" +
                "\t\t\t\t\tformat_version.values=[this['format_version'].value];\n" +
                "\t\t\t\t}\n" +
                "\t\t\t\tformat_version.sources=this['format_version'].sources;\n" +
                "\t\t\t\tresult.format_version=format_version;\n" +
                "\n" +
                "\t\t\t\tvar mimetype={};\n" +
                "\t\t\t\tmimetype.status=this['mimetype'].status;\n" +
                "\t\t\t\tif (this['mimetype'].status == 'CONFLICT'){\n" +
                "\t\t\t\t\tmimetype.values=this['mimetype'].values;\n" +
                "\t\t\t\t} else{\n" +
                "\t\t\t\t\tmimetype.values=[this['mimetype'].value];\n" +
                "\t\t\t\t}\n" +
                "\t\t\t\tmimetype.sources=this['mimetype'].sources;\n" +
                "\t\t\t\tresult.mimetype=mimetype;\n" +
                "\t\t}\n" +
                "\t    if (result!=null)  {\n" +
                "\t    \temit(result,1);\n" +
                "\t    }    \n" +
                "    }\t";

        String reduce = "function reduce(key, values) {" +
                "var res = 0;" +
                "values.forEach(function (v) {" +
                "res += v;" +
                "});" +
                "return res;" +
                "}";

        Iterator<Property> propertyIterator = pLayer.find(Property.class, null);
        List<String> properties=new ArrayList<>();
        while (propertyIterator.hasNext()){
            Property next = propertyIterator.next();
            if (!properties.contains(next.getKey()))
                properties.add(next.getKey());
        }

        Iterator<Source> sourceIterator = pLayer.find(Source.class, null);
        List<String> sources=new ArrayList<>();
        String header="";
        while (sourceIterator.hasNext()){
            Source next = sourceIterator.next();
            sources.add(next.getName()+":"+next.getVersion());
        }
        header+="Count;";
        for (String source: sources)
            header+="Format-"+source+";";

        for (String source: sources)
            header+="FormatVersion-"+source+";";

        for (String source: sources)
            header+="Mimetype-"+source+";";

        header+="SampleURL;Query";

        List<BasicDBObject> basicDBObjects = pLayer.mapReduce(map2, reduce, null);

        int size = basicDBObjects.size();
        System.out.print(size);
        List<BasicDBObject> result=new ArrayList<>();


        File file = new File("conflicts.csv");

        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        PrintWriter out = new PrintWriter(file);
        out.println(header);
        for (BasicDBObject obj: basicDBObjects)
        {
            String output="";
            Double count =  obj.getDouble("value");
            output+=count.intValue();
            BasicDBObject id1 = (BasicDBObject) obj.get("_id");
            if (id1.size()==0)  continue;
            //TODO: finish serializing this shiiit!;
            BasicDBObject format =(BasicDBObject) id1.get("format");
            BasicDBObject format_version =(BasicDBObject) id1.get("format_version");
            BasicDBObject mimetype =(BasicDBObject) id1.get("mimetype");

            BasicDBList format_values = (BasicDBList) format.get("values");
            BasicDBList format_sources = (BasicDBList) format.get("sources");
            output+=basicDBListsToCSV(format_values,format_sources,sources);

            BasicDBList format_version_values = (BasicDBList) format_version.get("values");
            BasicDBList format_version_sources = (BasicDBList) format_version.get("sources");
            output+=basicDBListsToCSV(format_version_values,format_version_sources,sources);

            BasicDBList mimetype_values = (BasicDBList) mimetype.get("values");
            BasicDBList mimetype_sources = (BasicDBList) mimetype.get("sources");
            output+=basicDBListsToCSV(mimetype_values,mimetype_sources,sources);

            BasicDBList andQuery=new BasicDBList();
            BasicDBObject query;
            query=new BasicDBObject();
            if (format.getString("status").equals("CONFLICT")){
                query.put("format.values", format_values);
            } else{
                query.put("format.value", format_values.get(0));
            }
            andQuery.add(query);
            query=new BasicDBObject();
            if (format_version.getString("status").equals("CONFLICT")){
                query.put("format_version.values", format_version_values);
            } else{
                query.put("format_version.value", format_version_values.get(0));
            }
            andQuery.add(query);
            query=new BasicDBObject();
            if (mimetype.getString("status").equals("CONFLICT")){
                query.put("mimetype.values", mimetype_values);
            } else{
                query.put("mimetype.value", mimetype.get(0));
            }
            andQuery.add(query);
            query=new BasicDBObject("$and", andQuery);
            String s = query.toString();
            Iterator<Element> elementIterator = pLayer.findQ(Element.class, query);
            output+=";";
            if (elementIterator.hasNext()){
                Element next = elementIterator.next();
                output+= "http://localhost:9000/c3po/objects/"+ next.getId();
            }
            output+=";"+s;
            out.println(output);
        }
    }

    public String basicDBListsToCSV(BasicDBList value, BasicDBList source,List<String> sources){

        String[] result=new String[sources.size()];
        for (int i=0; i< result.length; i++)
            result[i]="";

        int size = value.size();
        if (size>1) {
            for (int i = 0; i < value.size(); i++) {
                String valueConflicted = value.get(i).toString();
                Integer sourceID = Integer.parseInt(source.get(i).toString());
                result[sourceID] = valueConflicted;
            }
        } else
        {
            String valueConflicted = value.get(0).toString();
            for (int i=0; i< source.size(); i++){
                Integer sourceID = Integer.parseInt(source.get(i).toString());
                result[sourceID] = valueConflicted;
            }
        }
        String output="";
        for (int i=0; i< result.length; i++)
            output += ";"+result[i];
        return output;
    }
}
