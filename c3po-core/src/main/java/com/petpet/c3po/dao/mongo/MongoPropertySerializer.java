package com.petpet.c3po.dao.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Property;

/**
 * Serializes a {@link Property} object to a mongo {@link DBObject}.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoPropertySerializer implements MongoModelSerializer {

  /**
   * Serializes the given {@link Property} object to a mongo NoSQL document.
   * Note that if the object is null or not of type {@link Property}, null is
   * returned.
   */
  @Override
  public DBObject serialize(Object object) {
    BasicDBObject property = null;

    if (object != null && object instanceof Property) {
      Property p = (Property) object;

      property = new BasicDBObject();
      property.put("_id", p.getId());
      property.put("key", p.getKey());
      property.put("type", p.getType());

    }
    return property;
  }

}
