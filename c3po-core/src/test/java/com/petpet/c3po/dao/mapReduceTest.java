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

    //@Before
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


    //@Test
    public void mapReduceGenericTest() throws Exception {

        String map2 = "function map() {" +
                        "for (var property in this) {" +
                            "for (var property2 in this) {" +
                                "if ( property != null && property2 !=null && property != property2 && this[property].status != null && this[property2].status != null && this[property].status == 'CONFLICT' && this[property2].status == 'CONFLICT') {" +
                                    "emit([property,this[property].values,this[property].sources, property2,this[property2].values,this[property2].sources],1);"+
                                "}" +
                            "}" +
                       "}" +
                    "}";
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
        header+="Count;Property;";
        for (String source: sources)
            header+=source+";";



        header+="Property;";
        for (String source: sources)
            header+=source+";";

        header+="URL";



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
            BasicDBList id = (BasicDBList) obj.get("_id");
            String prop1 = id.get(0).toString();
            output+=";"+prop1;
            BasicDBList l1=(BasicDBList) (id.get(1));
            BasicDBList s1=(BasicDBList) (id.get(2));
            output+=basicDBListsToCSV(l1,s1,sources);
            /*for (Object o : l1){
                if (o.toString().length() >50)
                    continue;
                output+=","+o.toString();
            }
            BasicDBList s1=(BasicDBList) (id.get(2));
            for (Object o : s1){
                output+=","+o.toString();
            }*/
            String prop2 = id.get(3).toString();
            output+=";"+prop2;

            BasicDBList l2=(BasicDBList) (id.get(4));
            BasicDBList s2=(BasicDBList) (id.get(5));
            output+=basicDBListsToCSV(l2,s2,sources);
           /* BasicDBList l2=(BasicDBList) (id.get(4));
            for (Object o : l2){
                if (o.toString().length() >50)
                    continue;
                output+=","+o.toString();
            }
            BasicDBList s2=(BasicDBList) (id.get(5));
            for (Object o : s2){
                output+=","+o.toString();
            }*/
            BasicDBObject query=new BasicDBObject();
            query.put(prop1+".values", l1);
            query.append(prop2+".values", l2);
            String s = query.toString();
            Iterator<Element> elementIterator = pLayer.findQ(Element.class, query);
            if (elementIterator.hasNext()){
                Element next = elementIterator.next();
                output+=";" + "http://localhost:9000/c3po/objects/"+ next.getId();
            }
            out.println(output);
            //  System.out.println(output);
        }
    }

    public String basicDBListsToCSV(BasicDBList value, BasicDBList source,List<String> sources){

        String[] result=new String[sources.size()];
        for (int i=0; i< result.length; i++)
            result[i]="";

        int size = value.size();

        for (int i=0; i< value.size();i++){
            String valueConflicted = value.get(i).toString();
            Integer sourceID = Integer.parseInt( source.get(i).toString());
            result[sourceID]=valueConflicted;
        }
        String output="";
        for (int i=0; i< result.length; i++)
            output += ";"+result[i];
        return output;
    }



}
