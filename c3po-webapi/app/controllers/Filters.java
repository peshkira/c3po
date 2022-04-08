/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.BetweenFilterCondition;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.api.model.helper.filtering.PropertyFilterCondition;
import com.petpet.c3po.utils.Configurator;
import common.WebAppConstants;
import helpers.Graph;
import helpers.PropertyValuesFilter;
import helpers.SessionFilters;
import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Filters extends Controller {

    public static Result addCondition() {
        Logger.debug("Received an addCondition in filter");
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        Filter filter = Filters.getFilterFromSession();
        if (filter != null) {
            DynamicForm form = play.data.Form.form().bindFromRequest();
            String propertyName = form.get("filter");
            String propertyValueString = form.get("value");
            String t = form.get("type");
            if (t.equals("graph")) {
                int value = Integer.parseInt(propertyValueString);
                for (Graph gr : Overview.getAllGraphs().getGraphs()) {
                    if (gr.getProperty().equals(propertyName))
                        propertyValueString = gr.getKeys().get(value);
                }
            }
            if (propertyValueString.equals("Other")) {
                PropertyFilterCondition pfc=new PropertyFilterCondition();
                pfc.setProperty(propertyName);
                pfc.getValues().add("NOLONGTAIL");
                filter.getPropertyFilterConditions().add(pfc);
                //filter.addFilterCondition(new FilterCondition(propertyName, "NOLONGTAIL"));
                Filters.setFilterFromSession(filter);
                return ok();
                //return ok("Cannot show distribution for 'Rest' value");

            }
            PropertyFilterCondition pfc=new PropertyFilterCondition();
            pfc.setProperty(propertyName);
            pfc.getValues().add(propertyValueString);
            filter.getPropertyFilterConditions().add(pfc);
            //filter.addFilterCondition(new FilterCondition(propertyName, "NOLONGTAIL"));
            Filters.setFilterFromSession(filter);

            //filter.addFilterCondition(new FilterCondition(propertyName, propertyValue));
            //Filters.setFilterFromSession(filter);
            return ok();
        }

        return badRequest("No filter was found in the session\n");
    }

    /**
     * Gets all selected filters and returns them to the client, so that it can
     * reconstruct the page.
     *
     * @return
     */
    public static Result getConditions() {
        Logger.debug("Received a getConditions call in filter");
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        List<PropertyValuesFilter> result = new ArrayList<PropertyValuesFilter>();
        Filter filter = Filters.getFilterFromSession();

        Map<String, Object> map = new HashMap<String, Object>();


        //JsonNode jsonNode = toJSON(filter);


        return ok(play.libs.Json.toJson(toJSON(filter)));


       /* List<FilterCondition> fcs = filter.getConditions();
        for (FilterCondition fc : fcs) {
            Property p = persistence.getCache().getProperty(fc.getField());
            Object obj = fc.getValue();
            if ( obj instanceof Object[]){
                Object[] objArray = (Object[]) obj;
                for (Object o: objArray){
                    String v = "Unknown";
                    if (o != null) {
                        v = o.toString();
                    }
                    PropertyValuesFilter f = null;
                    String distributionType = StringParser.getDistributionType(v);
                    String distributionTypeWidth = StringParser.getDistributionTypeWidth(v);
                    PropertyValuesFilter pvf = Properties.getValues(p.getKey(), distributionType, distributionTypeWidth, v);
                    result.add(pvf);
                }

            } else{
                String v = "Unknown";
                if (obj != null) {
                    v = obj.toString();
                }
                PropertyValuesFilter f = null;
                String distributionType = StringParser.getDistributionType(v);
                String distributionTypeWidth = StringParser.getDistributionTypeWidth(v);
                PropertyValuesFilter pvf = Properties.getValues(p.getKey(), distributionType, distributionTypeWidth, v);
                result.add(pvf);
            }

        }
        return ok(play.libs.Json.toJson(result));*/
    }


    public static Filter getFilterFromQuery(Map<String, String[]> query) {
        Filter filter = new Filter();

        for (String key : query.keySet()) {
            String[] values = query.get(key);

            for (String val : values) {
                Object typedValue = Properties.getTypedValue(val);
                filter.addFilterCondition(new FilterCondition(key, typedValue));
            }
        }

        return filter;
    }

    public static Filter getFilterFromSession() {
        String session = session(WebAppConstants.SESSION_ID);
        return SessionFilters.getFilter(session);

    }

    public static void setFilterFromSession(Filter filter) {
        List<String> toPrint = new ArrayList<String>();
        String collectionName = null;
        for (FilterCondition fc : filter.getConditions()) {
            toPrint.add(fc.getField() + " : " + fc.getValue());
            if (fc.getField().equals("collection"))
                collectionName = fc.getValue().toString();
        }
        Logger.debug("Setting the filter session to: " + filter.toSRUString());

        if (collectionName == null)
            collectionName = "all";
        session().put(WebAppConstants.CURRENT_COLLECTION_SESSION, collectionName);
        String session = session(WebAppConstants.SESSION_ID);
        SessionFilters.addFilter(session, filter);
    }

    public static Graph getGraph(String property) {

        DynamicForm form = play.data.Form.form().bindFromRequest();
        String alg = form.get("alg");
        return Graph.getGraph(Filters.getFilterFromSession(), property);
    }

    public static Result removeCondition(String property, String value) {
        Logger.debug("Received a removeCondition call in filter, removing filter with property " + property);
        Filter filter = Filters.getFilterFromSession();
        for (Iterator<FilterCondition> iter = filter.getConditions().listIterator(); iter.hasNext(); ) {
            FilterCondition fc = iter.next();
            if (fc.getField().equals(property)) {
               /* Object obj = fc.getValue();
                if (obj instanceof Object[]) {
                    Object[] objArray = (Object[]) obj;
                    List<Object> objList = Arrays.asList(objArray);
                    Iterator<Object> iterator = objList.iterator();
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        String s = next.toString();
                        if (s.equals(value))
                            objList.remove(next);
                    }
                    fc.setValue(objList.toArray());
                } else*/
                iter.remove();
            }
        }
        Filters.setFilterFromSession(filter);
        return ok();
    }

    public static BetweenFilterCondition getBetweenFilterCondition(String string, String propertyName) {
        //String[] split = string.split(" \\|");
        String[] values = string.split("\\-");
        int left = Integer.parseInt(values[0]);
        int right = Integer.parseInt(values[1]);
        BetweenFilterCondition result = new BetweenFilterCondition(propertyName,
                BetweenFilterCondition.Operator.GTE,
                left,
                BetweenFilterCondition.Operator.LT,
                right);

        return result;
    }

    public static Filter normalize(Filter filter) {
        Filter result = new Filter();
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        List<FilterCondition> conditions = filter.getConditions();
        if (conditions == null || conditions.size() == 0)
            return filter;
        for (FilterCondition fc : conditions) {
            String property = fc.getField();
            Object value = fc.getValue();
            Property p = persistence.getCache().getProperty(property);
            if (p.getType().equals(PropertyType.INTEGER.toString()) || p.getType().equals(PropertyType.FLOAT.toString())) {
                if (value == null || value.toString().equals("Unknown"))
                    result.addFilterCondition(new FilterCondition(property, null));
                else {
                    BetweenFilterCondition bfc = getBetweenFilterCondition(value.toString(), property);
                    result.addFilterCondition(bfc);
                }
            }
            if (p.getType().equals(PropertyType.DATE.toString())) {
                if (value == null || value.toString().equals("Unknown"))
                    result.addFilterCondition(new FilterCondition(property, null));
                else {
                    Calendar c = Calendar.getInstance();
                    int year = Integer.parseInt(value.toString());
                    c.setTimeZone(TimeZone.getDefault());
                    c.set(year, 0, 1, 0, 1);
                    Date timeFrom = c.getTime();
                    c = Calendar.getInstance();
                    c.set(year, 11, 31, 23, 59);
                    Date timeTo = c.getTime();

/*
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date d1 = new ISODate dateFormat.parse("2014-01-01 00:00:00");
                    DateTimeFormatter year = ISODateTimeFormat.year();
                    Date d2 = dateFormat.parse("2014-01-01 00:00:05");*/
                    BetweenFilterCondition bfc = new BetweenFilterCondition(property,
                            BetweenFilterCondition.Operator.GTE,
                            timeFrom,
                            BetweenFilterCondition.Operator.LT,
                            timeTo);
                    result.addFilterCondition(bfc);
                }

            } else {
                if (value == null || value.toString().equals("Unknown"))
                    result.addFilterCondition(new FilterCondition(property, null));
                else
                    result.addFilterCondition(new FilterCondition(property, value));
            }
        }
        return result;
    }

    public static Result getSources() {
        List<String> sources = new ArrayList<String>();
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        Iterator<Source> sourceIterator = persistence.find(Source.class, null);
        while (sourceIterator.hasNext()) {
            sources.add(sourceIterator.next().toString());
        }
        return ok(play.libs.Json.toJson(sources));
    }

    public static Result apply() {
        JsonNode json = request().body().asJson();

        String SRU = toSRU(json);
        Filter f = null;
        try {
            if (SRU.equals(""))
                f = new Filter();
            else
                f = new Filter(SRU);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (f != null)
            setFilterFromSession(f);


        return ok();
    }


    private static Object toJSON(Filter filter) {

        List<Object> maps = new ArrayList<Object>();
        List<PropertyFilterCondition> propertyFilterConditions = filter.getPropertyFilterConditions();

        for (PropertyFilterCondition propertyFilterCondition : propertyFilterConditions) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("propertyname", propertyFilterCondition.getProperty());
            List<String> statuses = propertyFilterCondition.getStatuses();
            if (statuses.size() > 0)
                map.put("propertystatus", statuses.get(0));
            map.put("strict", propertyFilterCondition.getStrict());
            List<Object> sourcedValues = new ArrayList<Object>();
            for (Map.Entry<String, String> stringStringEntry : propertyFilterCondition.getSourcedValues().entrySet()) {
                Map<String, String> sv = new HashMap<String, String>();
                sv.put("propertyvalue", stringStringEntry.getValue());
                sv.put("propertyvaluesource", stringStringEntry.getKey());
                sourcedValues.add(sv);
            }

            for (String s : propertyFilterCondition.getValues()) {
                Map<String, String> sv = new HashMap<String, String>();
                sv.put("propertyvalue", s);
                sv.put("propertyvaluesource", "");
                sourcedValues.add(sv);
            }
            if (sourcedValues.size() > 0)
                map.put("sourcedvalues", sourcedValues);
            maps.add(map);
        }
        return maps;
    }

    private static String toSRU(JsonNode json) {
        String result = "";
        Iterator<JsonNode> iterator = json.iterator();
        while (iterator.hasNext()) {
            JsonNode next = iterator.next();
            String propertyname = next.get("propertyname").asText();
            if (result.equals(""))
                result += "property=" + propertyname;
            else
                result += "&property=" + propertyname;
            String propertystatus = next.get("propertystatus").asText();
            if (!propertystatus.equals(""))
                result += "&status=" + propertystatus;
            boolean strict = next.get("strict").asBoolean();
            if (strict)
                result += "&strict=true";
            JsonNode sourcedvalues = next.get("sourcedvalues");
            for (JsonNode sourcedvalue : sourcedvalues) {
                String propertyvalue = sourcedvalue.get("propertyvalue").asText();
                String propertyvaluesource = sourcedvalue.get("propertyvaluesource").asText();
                if (!propertyvaluesource.equals(""))
                    result += "&source=" + propertyvaluesource;
                if (!propertyvalue.equals(""))
                    result += "&value=" + propertyvalue;
            }
        }
        return result;
    }
}
