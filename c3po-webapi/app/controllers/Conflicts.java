package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
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
            String propertyName = condition.get("property").asText();
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
                if (mr.getProperty().getId().equals(property))
                {

                    if (mr.getStatus().equals("CONFLICT")){
                        int indexOf = mr.getValues().indexOf(value);
                        mr.getValues().remove(indexOf);
                        mr.getSources().remove(indexOf);
                    } else {
                        if (mr.getValue()!=null){
                            int i = sourceNames.indexOf(source);
                            int indexOf = mr.getSources().indexOf(String.valueOf(i));
                            mr.getSources().remove(indexOf);
                        }
                    }
                    ruleElement.getMetadata().add(mr);
                }


            }

        }



        rule.setElement(ruleElement);
        rule.setFilter(ruleFilter);
        rules.add(rule);
        System.out.println("data = " + json);
        saveRules();
        return ok();
    }

    public static Result deleteRule() {
        return play.mvc.Results.TODO;
    }
    public static void saveRules(){
        Logger.debug("Saving the rules");
        String path = System.getProperty( "user.home" ) + File.separator + ".C3POConflictRules";
        File file = new File(path);
        try
        {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(rules);
            out.close();
            fileOut.close();
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
            ObjectInputStream in = new ObjectInputStream(fileIn);
            tmpRules = (List<Rule>) in.readObject();
            in.close();
            fileIn.close();
        }catch(IOException i)
        {
            i.printStackTrace();
            return;
        }catch(ClassNotFoundException c)
        {
            Logger.debug("Rule class not found");
            c.printStackTrace();
            return;
        }
        rules.addAll(tmpRules);
        Logger.debug("The rules are loaded");
    }

    public static Result resolve() {
        ConflictResolutionProcessor crp=new ConflictResolutionProcessor();
        List<Rule> tmpRules=new ArrayList<Rule>();
        JsonNode json = request().body().asJson();
        Iterator<JsonNode> jsonNodeIterator = json.elements();
        /*while (jsonNodeIterator.hasNext()){
            JsonNode next = jsonNodeIterator.next();
            String ruleName = next.asText();
            Rule rule= getRuleByName(ruleName);
            if (rule!=null) {
                rule.setFilter(Filters.normalize(rule.getFilter()));
                tmpRules.add(rule);
            }
            else{
                tmpRules.addAll(rules);
            }
        } */
             tmpRules.addAll(rules);
        crp.setRules(tmpRules);
        long resolve = crp.resolve();
        return ok(String.valueOf(resolve) + " conflicts were resolved");
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
        String url = request().host();
        String filename= "conflicts_overview_table_" +session(WebAppConstants.SESSION_ID) + ".csv";
        String path = "exports"+File.separator +filename;

        File file = crp.printCSV(path, url);

        try {
            response().setContentType("text/csv");
            response().setHeader("Content-disposition","attachment; filename="+filename);
            return ok(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return internalServerError(e.getMessage());
        }
    }


}
