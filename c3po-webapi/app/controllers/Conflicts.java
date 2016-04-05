package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.petpet.c3po.analysis.conflictResolution.Rule;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.utils.Configurator;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import views.html.conflicts;
import views.html.index;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static play.mvc.Controller.request;
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
        loadRules();
        JsonNode jsonNode = Json.toJson(rules);
        return ok(play.libs.Json.toJson(rules));
    }


    public static Result createRule() {
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        JsonNode json = request().body().asJson();
        Rule rule=new Rule();
        Filter ruleFilter=new Filter();
        Element ruleElement=new Element(null,null);
        Iterator<JsonNode> jsonNodeIterator = json.elements();
        while (jsonNodeIterator.hasNext()){
            JsonNode next = jsonNodeIterator.next();
            String component = next.get(2).asText();
            String propertyName = next.get(0).asText();
            String propertyValue = next.get(1).asText();
            if (component.equals("action")){
                if (propertyName!=null) {
                    Property property = persistence.getCache().getProperty(propertyName);
                    MetadataRecord mr=new MetadataRecord(property,propertyValue);
                    ruleElement.getMetadata().add(mr);
                }
            } else if (component.equals("condition")){
                if (propertyName!=null) {
                    FilterCondition fc=new FilterCondition(propertyName,propertyValue);
                    ruleFilter.addFilterCondition(fc);
                }
            } else if (component.equals("name")) {
                if (propertyName != null) {
                    rule.setName(propertyName);
                }
            }
        }
        rule.setElement(ruleElement);
        rule.setFilter(ruleFilter);
        rules.add(rule);
        System.out.println("data = " + json);
        saveRules();
        loadRules();
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
        rules=tmpRules;
        Logger.debug("The rules are loaded");


    }
}
