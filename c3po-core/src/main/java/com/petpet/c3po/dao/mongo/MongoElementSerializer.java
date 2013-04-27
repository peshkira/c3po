package com.petpet.c3po.dao.mongo;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.MetadataRecord.Status;
import com.petpet.c3po.utils.DataHelper;

/**
 * Serializes {@link Element}s into {@link DBObject}s.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoElementSerializer implements MongoModelSerializer {

  /**
   * Maps the given element object to a mongo NoSQL schema, where every element
   * is represented as a single document wrapping all its meta data records.
   * 
   * Note that if the passed object is null not of type {@link Element} null is
   * returned.
   */
  @Override
  public DBObject serialize(Object object) {

    BasicDBObject document = null;

    if (object != null && object instanceof Element) {
      Element element = (Element) object;

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

      BasicDBObject meta = new BasicDBObject();
      for (MetadataRecord r : element.getMetadata()) {
        BasicDBObject key = new BasicDBObject();

        key.put("status", r.getStatus());

        if (r.getStatus().equals(Status.CONFLICT.name())) {
          BasicDBObject conflicting;
          List<Object> values;
          List<Object> sources;
          if (meta.containsField(r.getProperty().getId())) {
            conflicting = (BasicDBObject) meta.get(r.getProperty().getId());
            values = (List<Object>) conflicting.get("values");
            if (values == null) {
              values = new ArrayList<Object>();
              values.add(conflicting.get("value"));
            }
            sources = (List<Object>) conflicting.get("sources");
            values.add(DataHelper.getTypedValue(r.getProperty().getType(), r.getValue()));
            sources.add(r.getSources().get(0));

          } else {
            conflicting = new BasicDBObject();
            values = new ArrayList<Object>();
            sources = new ArrayList<Object>();

            values.add(DataHelper.getTypedValue(r.getProperty().getType(), r.getValue()));
            sources.add(r.getSources().get(0));
          }

          conflicting.put("values", values);
          conflicting.put("sources", sources);
          conflicting.put("status", r.getStatus());
          meta.put(r.getProperty().getId(), conflicting);

        } else {
          key.put("value", DataHelper.getTypedValue(r.getProperty().getType(), r.getValue()));
          key.put("sources", r.getSources());
          meta.put(r.getProperty().getId(), key);
        }

      }

      if (!meta.keySet().isEmpty()) {
        document.put("metadata", meta);
      }

    }

    return document;
  }

}
