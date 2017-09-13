package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.petpet.c3po.analysis.ConflictResolutionProcessor;
import com.petpet.c3po.analysis.conflictResolution.Rule;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.filtering.PropertyFilterCondition;
import play.Logger;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import views.html.conflicts;

import java.io.*;
import java.text.ParseException;
import java.util.*;

import static play.mvc.Controller.request;
import static play.mvc.Controller.response;
import static play.mvc.Results.*;

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
        JsonNode json = request().body().asJson();
        Rule rule=new Rule();
        String uid = json.get("uid").asText();

        Filter tmpFilter=new Filter();
        tmpFilter.addFilterCondition(new FilterCondition("uid", uid ));

        Filter ruleFilter=new Filter();

        JsonNode conditions = json.get("conditions");
        for (JsonNode condition: conditions){
            PropertyFilterCondition pfc=new PropertyFilterCondition();
            Iterator<String> stringIterator = condition.fieldNames();
            while(stringIterator.hasNext()){
                String name = stringIterator.next();
                String value = condition.get(name).asText();
                if (name.equals("Property")){
                    pfc.setProperty(value);

                } else if (name.equals("Status")){
                    List<String> statuses=new ArrayList<String>();
                    statuses.add(value);
                    pfc.setStatuses(statuses);
                } else {
                    if (!value.equals("null"))
                    pfc.getSourcedValues().put(name,value);
                }
            }
            ruleFilter.getPropertyFilterConditions().add(pfc);
        }
        List<MetadataRecord> metadataRecords=new ArrayList<MetadataRecord>();

        JsonNode valuesToDelete = json.get("valuesToDelete");
        Iterator<JsonNode> elements = valuesToDelete.elements();
        while (elements.hasNext()){
            JsonNode next = elements.next();
            String source = next.get("source").asText();
            String property = next.get("property").asText();
            String value = next.get("value").asText();
            MetadataRecord metadataRecord=new MetadataRecord();
            metadataRecord.setProperty(property);
            if (source.equals("Status")){ //process the case when we want to modify only the status of the property to 'RESOLVED'
                metadataRecord.setStatus("RESOLVED");
            } else {
                Map<String, String> sourcedValues=new HashMap<String, String>();
                sourcedValues.put(source,value);
                metadataRecord.setSourcedValues(sourcedValues);
            }
            metadataRecords.add(metadataRecord);
        }

        String ruleName = json.get("ruleName").asText();
        String ruleDescription = json.get("ruleDescription").asText();
        rule.setMetadataRecordList(metadataRecords);
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
            JsonNode jsonNode = Json.toJson(rules);
            String rulesJSON2 = jsonNode.toString();

            FileWriter fileWriter=new FileWriter(path);
            fileWriter.write(rulesJSON2);
            fileWriter.close();
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

    public static Result table() {
        DynamicForm form = play.data.Form.form().bindFromRequest();
        Collection<String> values = form.data().values();

        ConflictResolutionProcessor crp=new ConflictResolutionProcessor();
        Filter filter = Filters.getFilterFromSession();
        String url = request().host();


        String s = crp.printCSV(url, filter, values);
        String filenameSuffix="";
        for (String value : values) {
            filenameSuffix+="_" +value;
        }
        File file=new File(System.getProperty( "user.home" ) + File.separator +"conflicts" +filenameSuffix+".csv");
        try {
            FileWriter fw=new FileWriter(file);
            fw.write(s);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok(s);
    }

    public static Result csv() {
        DynamicForm form = play.data.Form.form().bindFromRequest();
        Collection<String> values = form.data().values();

        ConflictResolutionProcessor crp=new ConflictResolutionProcessor();
        Filter filter = Filters.getFilterFromSession();
        String url = request().host();


        String s = crp.printCSV(url, filter, values);
        File file=new File(System.getProperty( "user.home" ) + File.separator +"conflicts");
        try {
            FileWriter fw=new FileWriter(file);
            fw.write(s);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            response().setContentType("text/csv");
            response().setHeader("Content-disposition","attachment; filename="+"conflicts");
            return ok(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return internalServerError(e.getMessage());
        }
    }


    public static Result getOverview() {
        ConflictResolutionProcessor crp=new ConflictResolutionProcessor();
        Filter filter = Filters.getFilterFromSession();
        String url = request().host();
        Map<String, Integer> overview = crp.getOverview(url, filter);
        ObjectNode jsonNodes = Json.newObject();

        JsonNode jsonNode = Json.toJson(overview);
        return ok(jsonNode);
    }

    public static Result resolveNew(){
        Http.RequestBody body = request().body();
        String path = request().path();
        String uri = request().uri();
        uri=uri.replace(path+"?","");
        uri=uri.replace( "%2B", "+").replace("%20"," ");
        int i = uri.indexOf("&propertyToResolve");
        String filter=uri.substring(0,i);
        Filter f= null;
        try {
            f = new Filter(filter);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String resolvingQuery = uri.substring(i+1, uri.length() );
        String[] split = resolvingQuery.split("&");
        Map<String,String> resolutions=new HashMap<String, String>();

        String propertyToResolve=null ;
        String resolveTo=null;
        for (String s : split) {
            String[] split1 = s.split("=");
            String key = split1[0];
            String value = split1[1];


            if (key.equals("propertyToResolve")) {http://localhost:9000/c3po/conflicts/resolve?property=format&source=Droid:3.0&value=Rich Text Format&status=CONFLICT&propertyToResolve=format&resolveTo=Rich Text Format
                propertyToResolve = value;
            }
            else if (key.equals("resolveTo")) {
                resolveTo = value;
                if (propertyToResolve != null) {
                    resolutions.put(propertyToResolve,resolveTo);
                    propertyToResolve = null;
                }
            }
        }

        ConflictResolutionProcessor crp=new ConflictResolutionProcessor();
        long resolve = crp.resolve(f, resolutions);

        Logger.debug("Resolved conflicts: " + resolve);
        return redirect("/c3po/overview");
    }
}
