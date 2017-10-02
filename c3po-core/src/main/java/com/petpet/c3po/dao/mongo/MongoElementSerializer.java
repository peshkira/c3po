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
package com.petpet.c3po.dao.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.LogEntry;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Serializes {@link Element}s into {@link DBObject}s.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 */
public class MongoElementSerializer implements MongoModelSerializer {

    /**
     * Maps the given element object to a mongo NoSQL schema, where every element
     * is represented as a single document wrapping all its meta data records.
     * <p>
     * Note that if the passed object is null not of type {@link Element} null is
     * returned.
     */
    @Override
    public DBObject serialize(Object object) {

   /* BasicDBObject document = null;
    if ( object != null && object instanceof Element ) {
      Element element = (Element) object;
      Gson gson=new Gson();
      String s = gson.toJson(element);
      document = (BasicDBObject) JSON.parse(s);
    }
    return document;*/

        BasicDBObject document = null;

        if (object != null && object instanceof Element) {
            Element element = (Element) object;
            //updateStatus(element);
            document = new BasicDBObject();
            if (element.getName() != null && !element.getName().equals("")) {
                document.put("name", element.getName());
            }

            if (element.getUid() != null && !element.getUid().equals("")) {
                document.put("uid", element.getUid());
            }

            if (element.getCollection() != null && !element.getCollection().equals("")) {
                document.put("collection", element.getCollection());
            }

            List<DBObject> meta = new ArrayList<DBObject>();

            PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();

            Iterator<Property> propertyIterator = persistence.find(Property.class, null);
            while (propertyIterator.hasNext()) {
                Property next = propertyIterator.next();
                boolean propertyIsPresent=false;
                BasicDBObject key = new BasicDBObject();
                for (MetadataRecord r : element.getMetadata()){
                    if (r.getProperty().equals(next.getKey())){
                        key.put("property", r.getProperty());
                        key.put("status", r.getStatus());

                        List<DBObject> sourcedValues = new ArrayList<DBObject>();
                        String type = Configurator.getDefaultConfigurator().getPersistence().getCache().getProperty(r.getProperty()).getType();

                        for (Map.Entry<String, String> stringStringEntry : r.getSourcedValues().entrySet()) {
                            BasicDBObject sourcedValue = new BasicDBObject();
                            sourcedValue.put("source", stringStringEntry.getKey());
                            sourcedValue.put("value", DataHelper.getTypedValue(type, stringStringEntry.getValue()));
                            Object typedValue = DataHelper.getTypedValue(type, stringStringEntry.getValue());
                            if (type.equals(PropertyType.DATE.name()) && typedValue instanceof String)
                                break;
                            sourcedValues.add(sourcedValue);
                        }
                        if (sourcedValues.size() > 0) {
                            key.put("sourcedValues", sourcedValues);
                        }
                        break;
                    }

                }
                meta.add(key);

            }

            document.put("metadata", meta);

            //if ( !meta.keySet().isEmpty() ) {
            //  document.put( "metadata", meta );
            //}

            BasicDBObject logEntries = new BasicDBObject();

            for (LogEntry logEntry : element.getLogEntries()) {

                logEntries.put("property", logEntry.getMetadataProperty());
                logEntries.put("valueOld", logEntry.getMetadataValueOld());
                logEntries.put("changeType", logEntry.getChangeType().name());
                logEntries.put("ruleName", logEntry.getRuleName());
            }
            if (logEntries.size() != 0)
                document.put("log", logEntries);

        }

        return document;
    }


}
