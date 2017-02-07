/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.petpet.c3po.api.model.helper;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.helper.filtering.PropertyFilterCondition;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used to define a filter for a c3po data model class. It has a
 * list of filter conditions that should be applied on the data before executing
 * a query. This allows the drill down into the data.
 * <p>
 * The persistence layer provider should interpret the conditions by logically
 * concatenating all conditions with an AND. If two or more filter conditions
 * apply to the same property, then they should be applied with a logical OR.
 * Consider the following example where we want to filter all objects from a
 * collection 'A' that have either text/html or text/xml mimetype. In such a
 * case the filter that will be passed to the {@link PersistenceLayer} will have
 * three {@link FilterCondition} objects: <br><br>
 * <p>
 * first - property is 'collection' and value is 'A' <br>
 * second - property is 'mimetype' and value is 'text/html' <br>
 * third - property is 'mimetype' and value is 'text/xml' <br><br>
 * <p>
 * Consider now that the same filter has to be applied but it has go over both
 * collection 'A' and collection 'B'. The caller has to include an additional
 * {@link FilterCondition} where the property is 'collection' and the value is
 * 'B' and the interpreter of this filter should use a logical OR for the
 * collection property as well.
 * <p>
 * Additionally, if a condition of the filter has a null value for a given
 * field, then this has to be interpreted as: where any given value exists for
 * this property. For example, if a condition has a property 'mimetype' and a
 * value 'null', then the filter should interpret where the searched objects
 * have a mimetype field that exists.
 * <p>
 * Note that a filter might also contain a {@link BetweenFilterCondition} object
 * for numeric properties and this should be interpreted accordingly.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 */
public class Filter implements Serializable {

    /**
     * A list of filter conditions, that has to be interpreted by the persistence
     * provider to a query or addition to a query.
     */
    private List<FilterCondition> conditions;

    public List<PropertyFilterCondition> getPropertyFilterConditions() {
        if (propertyFilterConditions==null)
            propertyFilterConditions=new ArrayList<PropertyFilterCondition>();
        return propertyFilterConditions;
    }

    public void setPropertyFilterConditions(List<PropertyFilterCondition> propertyFilterConditions) {
        this.propertyFilterConditions = propertyFilterConditions;
    }

    private List<PropertyFilterCondition> propertyFilterConditions;

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    private boolean strict=false;

    private String raw;

    /**
     * Create a new empty filter with an initialized empty list of
     * {@link FilterCondition}s.
     */
    public Filter() {
        conditions = new ArrayList<FilterCondition>();
        propertyFilterConditions=new ArrayList<PropertyFilterCondition>();
    }

    /**
     * Creates a new filter and adds the given conditions to the filter.
     *
     * @param fc the condition to add.
     */
    public Filter(FilterCondition fc) {
        this();
        this.conditions.add(fc);
    }

    public Filter(String SRUString) throws ParseException {
        this();
        if (SRUString.contains("&strict=true")) {
            setStrict(true);
            SRUString=SRUString.replace("&strict=true","");
        }
        SRUString=SRUString.replace( "%2B", "+").replace("%20"," ");
        int i = SRUString.indexOf("&property");
        while (i > -1){
            String substring = SRUString.substring(0, i);
            PropertyFilterCondition pfc=new PropertyFilterCondition(substring);
            propertyFilterConditions.add(pfc);
            SRUString=SRUString.replace(substring+"&","");
            i = SRUString.indexOf("&property");
        }
        PropertyFilterCondition pfc=new PropertyFilterCondition(SRUString);
        propertyFilterConditions.add(pfc);
    }

    /**
     * Creates a new filter and sets the {@link FilterCondition}s to the passed
     * list.
     *
     * @param fcs the filter conditions to use.
     */
    public Filter(List<FilterCondition> fcs) {
        conditions = fcs;
    }

    /**
     * Copies the given filter to this filter.
     *
     * @param another the filter to copy.
     */
    public Filter(Filter another) {
        this.conditions = new ArrayList<FilterCondition>(another.conditions);
        this.propertyFilterConditions=new ArrayList<PropertyFilterCondition>(another.getPropertyFilterConditions());
    }

    public List<FilterCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<FilterCondition> conditions) {
        this.conditions = conditions;
    }

    /**
     * Adds a new filter condition unless it is null or it already exists.
     *
     * @param fc the filter condition to add.
     */
    public void addFilterCondition(FilterCondition fc) {
        if (conditions == null) {
            conditions = new ArrayList<FilterCondition>();
        }

        if (fc != null && !conditions.contains(fc))
            conditions.add(fc);
    }

    /**
     * Create a new filter that contains only the {@link FilterCondition} objects
     * that are part of the fields array. However, if the new filter has no filter
     * condition, then the current filter is returned.
     *
     * @param fields the fields that should remain in the subfilter.
     * @return the new filter or the same if none of the conditions were matching.
     */
    public Filter subFilter(String... fields) {
        Filter subFilter = new Filter();
        List<String> fieldList = Arrays.asList(fields);

        for (FilterCondition fc : this.conditions) {
            if (fieldList.contains(fc.getField())) {
                subFilter.addFilterCondition(fc);
            }
        }

        return (subFilter.getConditions().size() > 0) ? subFilter : this;
    }

    /**
     * Returns true if this filter has at least one {@link FilterCondition} with
     * the given field.
     *
     * @param field the field to check for.
     * @return true if it contains such a filter condition, false otherwise.
     */
    public boolean contains(String field) {
        for (FilterCondition fc : this.conditions) {
            if (fc.getField().equals(field)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if it contains the given {@link FilterCondition}. Note that the
     * values are checked for equality as well.
     *
     * @param fc the {@link FilterCondition} to check.
     * @return true if it contains this {@link FilterCondition}, false otherwise.
     */
    public boolean contains(FilterCondition fc) {
        return this.conditions.contains(fc);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Filter other = (Filter) obj;
        if (conditions == null) {
            if (other.conditions != null)
                return false;
        } else if (!conditions.equals(other.conditions))
            return false;
        return true;
    }

    public String toString() {
        String result = "";
        for (FilterCondition condition : conditions) {
            result += condition.getField() + ":" + condition.getValue() + ",";
        }
        if (conditions.size() > 0)
            result = result.substring(0, result.length() - 1);

        return result;

    }

    public String toSRUString(){
        String result="";

        for (PropertyFilterCondition pfc: propertyFilterConditions){
            result +=pfc.toSRUString();
        }
        if (isStrict()){
            result+="strict=true&";
        }
        if (result.endsWith("&"))
            result=result.substring(0,result.length()-1);
        result=result.replace("+", "%2B").replace(" ", "%20");
        return result;


    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }
}
