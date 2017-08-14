package com.petpet.c3po.api.model.helper.filtering;

import java.text.ParseException;
import java.util.*;

/**
 * Created by artur on 09/12/2016.
 * <p>
 * This class describes an advanced filter condition, useful when operating with complex queries. Due to high granulary of the possible values, these conditions make possible to capture objects accurately.
 */
public class PropertyFilterCondition {
    /**
     * This field contains key-value pairs (source, value) for properties. During serialisation each pair must be united using logical AND.
     */
    Map<String, String> sourcedValues;
    /**
     * This field contains possible values for property status. During serialisation each status must be united using logical OR.
     */
    List<String> statuses;
    /**
     * This field contains property name.
     */
    String property;
    /**
     * This field contains possible property values and useful when the source is unknown. During serialisation each property value must be united using logical OR.
     */
    List<String> values;
    /**
     * This field contains possible property value sources and useful when the value is unknown. During serialisation each property value source must be united using logical OR.
     */
    List<String> sources;

    public PropertyFilterCondition() {
        sourcedValues = new HashMap<String, String>();
        statuses = new ArrayList<String>();
        property = new String();
        values = new ArrayList<String>();
        sources = new ArrayList<String>();
    }
    public PropertyFilterCondition(String SRUString) throws ParseException{
        this();
        try {
            String[] split = SRUString.split("&");
            String propSource = null;
            String propValue = null;
            for (String s : split) {
                String[] subSplit = s.split("=");
                String key = subSplit[0];
                String value = subSplit[1];
                if (key.equals("property")) {
                    setProperty(value);
                }
                else if (key.equals("status")) {
                    getStatuses().add(value);
                }
                else if (key.equals("source")) {
                    propSource = value;
                }
                else if (key.equals("value")) {
                    propValue = value;
                    if (propSource != null) {
                        getSourcedValues().put(propSource, propValue);
                        propSource = null;
                    }
                    else {
                        getValues().add(value);
                    }
                }
            }
        }
        catch (Exception e){
            throw new ParseException("Could not parse the string to PropertyFilterCondition",0);
        }
    }

    public String toSRUString() {
        String result="";
        result+="property"+"="+neutralizeStringForSRU(getProperty());
        result+="&";
        for (String status : getStatuses()) {
            result+="status"+"="+neutralizeStringForSRU(status);
            result+="&";
        }
        for (Map.Entry<String, String> stringStringEntry : sourcedValues.entrySet()) {
            String source = stringStringEntry.getKey();
            String value = stringStringEntry.getValue();
            result+="source"+"="+neutralizeStringForSRU(source);
            result+="&";
            result+="value"+"="+neutralizeStringForSRU(value);
            result+="&";
        }
        for (String value : getValues()) {
            result+="value"+"="+neutralizeStringForSRU(value);
            result+="&";
        }
        return result;
    }

    public static enum PropertyFilterConditionType {
        STATUS,
        PROPERTY,
        VALUE,
        SOURCE,
        SOURCEDVALUE
    }

    private String neutralizeStringForSRU(String input){
        return input.replace("="," ").replace("&", " ");
    }

    public void addCondition(PropertyFilterConditionType type, Object value) {
        switch (type) {
            case PROPERTY:
                property = value.toString();
                break;
            case SOURCE:
                if (!sources.contains(value.toString()))
                    sources.add(value.toString());
                break;
            case STATUS:
                if (!statuses.contains(value.toString()))
                    statuses.add(value.toString());
                break;
            case VALUE:
                if (!values.contains(value.toString()))
                    values.add(value.toString());
                break;
            case SOURCEDVALUE:
                Set<Map.Entry<String, String>> entries = ((Map<String, String>) value).entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    if (!sourcedValues.containsKey(entry.getKey()))
                        sourcedValues.put(entry.getKey(), entry.getValue());
                }
                break;
        }
    }


    public void deleteCondition(PropertyFilterConditionType type, Object value) {
        switch (type) {
            case PROPERTY:
                property = new String();
                break;
            case SOURCE:
                if (sources.contains(value.toString()))
                    sources.remove(value.toString());
                break;
            case VALUE:
                if (values.contains(value.toString()))
                    values.remove(value.toString());
                break;
            case SOURCEDVALUE:
                Set<Map.Entry<String, String>> entries = ((Map<String, String>) value).entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    if (sourcedValues.containsKey(entry.getKey()))
                        sourcedValues.remove(entry.getKey());
                }
                break;
        }
    }

    public Map<String, String> getSourcedValues() {
        return sourcedValues;
    }

    public void setSourcedValues(Map<String, String> sourcedValues) {
        this.sourcedValues = sourcedValues;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }
}
