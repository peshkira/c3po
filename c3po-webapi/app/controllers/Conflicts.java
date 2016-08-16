package controllers;

import ch.qos.logback.core.util.FileUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.petpet.c3po.analysis.ConflictResolutionProcessor;
import com.petpet.c3po.analysis.conflictResolution.Rule;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.utils.Configurator;
import common.WebAppConstants;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import views.html.conflicts;

import javax.swing.plaf.metal.MetalRadioButtonUI;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static play.mvc.Controller.request;
import static play.mvc.Controller.response;
import static play.mvc.Controller.session;
import static play.mvc.Results.internalServerError;
import static play.mvc.Results.ok;
/**
 * Created by artur on 01/04/16.
 */
public class Conflicts {
    static List<Rule> rules=new ArrayList<Rule>();

    public static Result index() {
        return ok(conflicts.render("c3po", Properties.getCollectionNames()));
    }

    public static Result getRules() {
        if (rules.isEmpty())
            loadRules();
        JsonNode jsonNode = Json.toJson(rules);
        return ok(jsonNode);
    }

    public static Result createRule() {
        MongoPersistenceLayer persistence = (MongoPersistenceLayer) Configurator.getDefaultConfigurator().getPersistence();
        JsonNode json = request().body().asJson();
        Rule rule=new Rule();
        String uid = json.get("uid").asText();

        Filter tmpFilter=new Filter();
        tmpFilter.addFilterCondition(new FilterCondition("uid", uid ));
        DBCursor cursor = persistence.findRaw(Element.class, tmpFilter);
        JsonNode elementJson=null;
        if (cursor.hasNext()) {
            DBObject next = cursor.next();
            String jsonStr = JSON.serialize( next );
            elementJson = Json.parse(jsonStr);
        }
        JsonNode conditions = json.get("conditions");
        ArrayNode filterArray=new ArrayNode(new JsonNodeFactory(false));
        for (JsonNode condition: conditions){
            String propertyName = condition.get("Property").asText();
            JsonNode propertyValueJson = elementJson.get(propertyName);
            ObjectNode tmp=Json.newObject();
            tmp.put(propertyName, propertyValueJson);
            filterArray.add(tmp);
        }

        ObjectNode andQuery=Json.newObject();

        andQuery.put("$and", filterArray);


        Filter ruleFilter=new Filter();
        ruleFilter.setRaw(andQuery.toString());

        Element ruleElement=new Element(null,null);


        Element element= null;
        Iterator<Element> elementIterator = persistence.find(Element.class, tmpFilter);

        if (elementIterator.hasNext()) {
            element=elementIterator.next();
        }

        JsonNode valuesToDelete = json.get("valuesToDelete");
        Iterator<Source> sourceIterator = persistence.find(Source.class, null);
        List<String> sourceNames=new ArrayList<String>();
        while (sourceIterator.hasNext()){
            Source next = sourceIterator.next();
            sourceNames.add(next.getName() + " (" + next.getVersion()+ ")");
        }
        Iterator<JsonNode> elements = valuesToDelete.elements();
        while (elements.hasNext()){
            JsonNode next = elements.next();
            String source = next.get("source").asText();
            String property = next.get("property").asText();
            String value = next.get("value").asText();


            for (MetadataRecord mr : element.getMetadata()){
                if (mr.getProperty().equals(property))
                {

                    if (mr.getStatus().equals("CONFLICT")){
                        int indexOf = mr.getValues().indexOf(value);
                        if (indexOf>=0){
                            mr.getValues().remove(indexOf);
                            mr.getSources().remove(indexOf);
                            if (mr.getValues().size()==1)
                                mr.setStatus("SINGLE_RESULT");
                        }

                    } else {
                        if (mr.getValues()!=null){
                            int i = sourceNames.indexOf(source);
                            int indexOf = mr.getValues().indexOf(i);
                            mr.getSources().remove(indexOf);
                            mr.getValues().remove(indexOf);
                            if (mr.getValues().size()==1)
                                mr.setStatus("SINGLE_RESULT");
                        }
                    }
                    ruleElement.getMetadata().add(mr);
                }
            }

        }

        String ruleName = json.get("ruleName").asText();
        String ruleDescription = json.get("ruleDescription").asText();
        rule.setElement(ruleElement);
        rule.setFilter(ruleFilter);
        rule.setName(ruleName);
        rule.setDescription(ruleDescription);
        if (rules.isEmpty())
            loadRules();
        rules.add(rule);
        System.out.println("data = " + json);
        saveRules();
        return ok();
    }

    public static Result deleteRule() {
        List<Rule> tmpRules=new ArrayList<Rule>();
        JsonNode json = request().body().asJson();
        Iterator<JsonNode> jsonNodeIterator = json.elements();
        int count=0;
        while (jsonNodeIterator.hasNext()){

            JsonNode next = jsonNodeIterator.next();
            String ruleName = next.asText();

            Iterator<Rule> iterator = rules.iterator();
            while (iterator.hasNext()){
                Rule next1 = iterator.next();
                if (next1.getName().equals(ruleName)) {
                    iterator.remove();
                    count++;
                }

            }

        }
        return ok(String.valueOf(count) + " rule/-s were removed.");
    }
    public static void saveRules(){
        Logger.debug("Saving the rules");
        String path = System.getProperty( "user.home" ) + File.separator + ".C3POConflictRules";
        File file = new File(path);
        try
        {
            //FileOutputStream fileOut = new FileOutputStream(file);
            JsonNode jsonNode = Json.toJson(rules);
            String rulesJSON2 = jsonNode.toString();

            FileWriter fileWriter=new FileWriter(path);
            fileWriter.write(rulesJSON2);
            fileWriter.close();
            //ObjectOutputStream out = new ObjectOutputStream(fileOut);
           // out.writeObject(rules);
            //out.close();
            //fileOut.close();
            Logger.debug("The rules are saved to :" + path);
        }catch(IOException i)
        {
            i.printStackTrace();
        }
    }
    public static void loadRules()
    {
        Logger.debug("loading the rules");
        List<Rule> tmpRules = null;
        String path = System.getProperty( "user.home" ) + File.separator + ".C3POConflictRules";
        File file = new File(path);
        if (!file.exists())
            return;
        try
        {
            FileInputStream fileIn = new FileInputStream(file);

            BufferedReader reader = new BufferedReader(new InputStreamReader(fileIn));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            System.out.println(out.toString());   //Prints the string content read from input stream
            reader.close();
            JsonNode parse = Json.parse(out.toString());
            Rule[] rules = Json.fromJson(parse, Rule[].class);
            tmpRules= Lists.newArrayList(rules);

            //  FileReader fileReader=new FileReader(path);
          //  ObjectInputStream in = new ObjectInputStream(fileIn);
           // tmpRules = (List<Rule>) in.readObject();
           // in.close();
           // fileIn.close();
        }catch(IOException i)
        {
            i.printStackTrace();
            return;
        }
        rules.addAll(tmpRules);
        Logger.debug("The rules are loaded");
    }

    public static Result resolve() {
        ConflictResolutionProcessor crp=new ConflictResolutionProcessor();
        List<Rule> tmpRules=new ArrayList<Rule>();
        Filter filter = Filters.getFilterFromSession();
        JsonNode json = request().body().asJson();
        Iterator<JsonNode> jsonNodeIterator = json.elements();
        while (jsonNodeIterator.hasNext()){
            JsonNode next = jsonNodeIterator.next();
            String ruleName = next.asText();
            Rule rule= getRuleByName(ruleName);
            if (rule!=null) {
                rule.setFilter(Filters.normalize(rule.getFilter()));
                tmpRules.add(rule);
            }
        }
        crp.setRules(tmpRules);
        long resolve = crp.resolve(filter);
        return ok(String.valueOf(resolve) + " conflict/-s were resolved");
    }

    static Rule getRuleByName(String ruleName){
        for(Rule rule: rules){
            if (rule.getName().equals(ruleName))
                return rule;
        }
        return null;
    }

    public static Result csv() {
        ConflictResolutionProcessor crp=new ConflictResolutionProcessor();
        Filter filter = Filters.getFilterFromSession();
        String url = request().host();
        String filename= "conflicts_overview_table_" +session(WebAppConstants.SESSION_ID) + ".csv";
        String path = System.getProperty( "user.home" ) + File.separator +filename;

        File file = crp.printCSV(path, url, filter);

        try {
            response().setContentType("text/csv");
            response().setHeader("Content-disposition","attachment; filename="+filename);
            return ok(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return internalServerError(e.getMessage());
        }
    }


}
