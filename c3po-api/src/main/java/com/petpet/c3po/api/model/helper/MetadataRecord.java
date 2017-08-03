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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single metadata record of an element.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 */
public class MetadataRecord implements Serializable {

    /**
     * The status of the element shows the certainty with which the value for the
     * given property is correct.
     *
     * @author Petar Petrov <me@petarpetrov.org>
     */
    public static enum Status {
        /**
         * Means that more than one tools confirm the value.
         */
        OK,

        /**
         * Only one tool has given this value.
         */
        SINGLE_RESULT,

        /**
         * One, two or more tools have provided different values for the same
         * property.
         */
        CONFLICT
    }

    /**
     * The property to which the value of this record belongs.
     */
    private String property;

    /**
     * A list for the conflicting values;
     */
    private List<String> values;

    /**
     * The status of the value.
     *
     * @see Status
     */
    private String status;

    /**
     * A list of sources that have measured the value.
     */
    private List<String> sources;


    public Map<String, String> getSourcedValues() {
        return sourcedValues;
    }

    public void setSourcedValues(Map<String, String> sourcedValues) {
        this.sourcedValues = sourcedValues;
    }

    private Map<String, String> sourcedValues;

    /**
     * Creates an empty record with a status ok.
     */
    public MetadataRecord() {
        this.sources = new ArrayList<String>();
        this.status = Status.OK.name();
        this.values = new ArrayList<String>();
        this.sourcedValues = new HashMap<String, String>();
    }

    /**
     * Creates an record for the given property with the given value and a status
     * SINGLE_RESULT.
     *
     * @param p
     * @param value
     */
    public MetadataRecord(String p, String value) {
        this();
        this.property = p;
        this.values.add(value);
    }

    public MetadataRecord(String p, String value, String sourceID) {
        this();
        this.property = p;
        this.values.add(value);
        this.sources.add(sourceID);
        this.sourcedValues.put(sourceID, value);

    }
    public MetadataRecord(String p, String value, String sourceID, String status) {
        this();
        this.property = p;
        this.values.add(value);
        this.sources.add(sourceID);
        this.sourcedValues.put(sourceID, value);
        this.status = status;

    }


    /**
     * Creates a record for the given property with the given value and the given
     * status.
     *
     * @param p
     * @param value
     * @param status
     */
    public MetadataRecord(String p, String value, Status status) {
        this(p, value);
        this.status = status.name();
    }

    public String getProperty() {
        return this.property;
    }

    public void setProperty(String p) {
        this.property = p;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getSources() {
        return new ArrayList<String>(sourcedValues.keySet());


        //   return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public List<String> getValues() {
        return new ArrayList<String>(sourcedValues.values());
        //if ( values == null ) {
        //  this.values = new ArrayList<String>();
        // }
        // return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "MetadataRecord [property=" + property + ", values=" + values
                + ", status=" + status + ", sources=" + sources + ", sourcedValues=" + sourcedValues + "]";
    }


}
