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
package com.petpet.c3po.analysis;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A CSV generator that creates a sparse matrix view of the data, where each
 * column is a property and each row is an element identifier and each cell has
 * the corresponding value.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 */
public class CSVGenerator {

    /**
     * Default logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CSVGenerator.class);

    /**
     * The persistence layer.
     */
    private PersistenceLayer persistence;

    /**
     * Creates the generator with the given persistence layer.
     *
     * @param p
     */
    public CSVGenerator(PersistenceLayer p) {
        this.persistence = p;
    }

    /**
     * Exports all the data from the given collection to a sparse matrix view
     * where each column is a property and each row is an element with the values
     * for the corresponding property.
     *
     * @param collection the collection to export
     * @param output     the output file
     */
    public void exportAll(String collection, String output) {
        final Iterator<Element> matrix = this.buildMatrix(collection);
        final Iterator<Property> allprops = this.persistence.find(Property.class, null);
        final List<Property> props = this.getProperties(allprops);

        this.write(matrix, props, output);
    }

    /**
     * Exports all the given properties for the given mimetype to a sparse matrix
     * view where each column is a property and each row is an element with the
     * values for the corresponding property.
     *
     * @param collection the collection to export
     * @param props      the properties filter
     * @param output     the output file.
     */
    public void export(final String collection, final List<Property> props, String output) {
        final Iterator<Element> matrix = this.buildMatrix(collection, props);

        this.write(matrix, props, output);
    }

    /**
     * Exports all the data matching the given filter to the given output file.
     *
     * @param filter the filter to match.
     * @param output the output file.
     */
    public void export(final Filter filter, String output) {
        final Iterator<Property> allprops = this.persistence.find(Property.class, null);
        final List<Property> props = this.getProperties(allprops);

        this.export(filter, props, output);
    }

    /**
     * Exports all the given properties and data that matches the given filter to
     * the given output file.
     *
     * @param filter the filter to apply.
     * @param props  the properties to generate.
     * @param output the output file.
     */
    public void export(final Filter filter, final List<Property> props, String output) {
        Iterator<Element> matrix = this.persistence.find(Element.class, filter);

        this.write(matrix, props, output);
    }

    /**
     * Writes a file containing the data of all elements in the matrix iterator
     * over all properties to the given output file.
     *
     * @param matrix the elements that the csv file should contain
     * @param props  the properties that the csv file should contain
     * @param output the output file where to write to.
     */
    public void write(Iterator<Element> matrix, List<Property> props, String output) {
        try {
            final File file = new File(output);
            LOG.info("Will export data in {}", file.getAbsolutePath());
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            StringBuilder result = new StringBuilder();
            FileWriter writer = new FileWriter(file);

            List<Source> sourceList = getSourceList();

            // build header of csv
            result.append("uid,");
            for (Property p : props) {
                result.append(p.getKey() + "(status)" + ",");
                for (Source source : sourceList) {
                    result.append(p.getKey() + "(" + source.toString() + ")" + ",");
                }

            }
            result.append(" \n ");
            boolean found = false;
            // for all elements append the values in the correct column
            while (matrix.hasNext()) {
                Element next = matrix.next();
                List<MetadataRecord> mrs = next.getMetadata();
                // first the uid
                result.append(replace(next.getUid()) + ",");

                // then the properties
                for (Property p : props) {
                    String serialize = serialize(p, mrs, sourceList);
                    result.append(serialize);

                }
                result.append(" \n ");

            }
            writer.write(result.toString());
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String serialize(Property p, List<MetadataRecord> mrs, List<Source> sourceList) {
        String result = "";
        String status="";
        for (MetadataRecord mr : mrs) {
            if (mr.getProperty().equals(p.getKey())) {
                status=mr.getStatus() == null ? "" : mr.getStatus();
                break;
            }
        }
        result=status+",";

        for (Source source : sourceList) {
            //int indexOfValue = -1;
            Object value = null;

            for (MetadataRecord mr : mrs) {
                if (mr.getProperty().equals(p.getKey())) {
                    for (String s : mr.getSources()) {
                        if (s.equals(source.toString())) {
                            value = mr.getValues().get(mr.getSources().indexOf(s));
                            break;
                        }
                    }
                    break;
                }
            }


            if (value != null) {
                String v = value.toString();
                result += replace(v) + ",";
            } else
                result += ",";
        }
        return result;

    }

    private List<Source> getSourceList() {
        List<Source> result = new ArrayList<Source>();
        Iterator<Source> sourceIterator = this.persistence.find(Source.class, new Filter());
        while (sourceIterator.hasNext()) {
            result.add(sourceIterator.next());
        }
        return result;
    }




    /**
     * Queries the db and obtains all elements for the given collection.
     *
     * @param collection the name of the colleciton.
     * @return the matching elements.
     */
    private Iterator<Element> buildMatrix(final String collection) {
        Filter filter = null;
        if (collection != null)
            filter = new Filter(new FilterCondition("collection", collection));
        return this.persistence.find(Element.class, filter);
    }

    /**
     * Queries the db and obtains all elements for the given collection provided
     * they have a value for all properties in the list.
     *
     * @param collection the name of the collection.
     * @param props      the properties to look for.
     * @return the matching elements.
     */
    private Iterator<Element> buildMatrix(final String collection, List<Property> props) {
        Filter filter = new Filter(new FilterCondition("collection", collection));

        for (Property p : props) {
            filter.addFilterCondition(new FilterCondition(p.getId(), null));
        }

        return this.persistence.find(Element.class, filter);
    }

    /**
     * Gets the value for the given list of meta data rocords or 'CONFLICT' if
     * there are more values.
     *
     * @param value the list of records.
     * @return the output string for the csv cell.
     */
    private String getValueFromMetaDataRecord(MetadataRecord mr) {
        String result = "";
    /*if ( mr.getValues().size() != 0 ) {
        List<String> values = mr.getValues();
        List<String> sources = mr.getSources();
        for (int i=0; i< values.size();i++){
          String s = values.get(i);
          Source source = this.persistence.getCache().getSource(sources.get(i));
          if (source==null)
            result+=s+"[null]"+";";
           else
          result+=replace(s)+"["+source.getName()+":"+source.getVersion()+"]"+";";
        }
        result = result.substring(0, result.length() - 1);


      result = (mr.getStatus().equals("CONFLICT")) ? "CONFLICT" : replace( values.get(0).toString() );
    }*/
        result = (mr.getStatus().equals("CONFLICT")) ? "CONFLICT" : replace(mr.getValues().get(0).toString());
        return result;
    }

    /**
     * Extracts {@link Property} objects from the given cursor and only sets the
     * id and the name field.
     *
     * @param cursor the iterator over the properties
     * @return a list of properties or an empty list.
     */
    public List<Property> getProperties(final Iterator<Property> cursor) {
        final List<Property> result = new ArrayList<Property>();

        while (cursor.hasNext()) {
            final Property next = cursor.next();
            result.add(next);
        }

        return result;
    }

    /**
     * replaces all comma ocurrences in the values with an empty string.
     *
     * @param str the string to check
     * @return a new altered string or an empty string if the input was null.
     */
    private String replace(String str) {
        return (str == null) ? "" : str.replaceAll(",", "").replaceAll("\n", " ");
    }

}
