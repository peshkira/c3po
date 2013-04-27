package com.petpet.c3po.dao.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Source;

/**
 * Serializes {@link Source} objects to a Mongo {@link DBObject}.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoSourceSerializer implements MongoModelSerializer {

  /**
   * Maps the given {@link Source} object to a mongo {@link DBObject}. Note that
   * if the given object is null or not of type {@link Source} then null is
   * returned.
   */
  @Override
  public DBObject serialize(Object object) {
    BasicDBObject source = null;

    if (object != null && object instanceof Source) {
      Source s = (Source) object;
      source = new BasicDBObject();

      if (s.getId() != null && !s.getId().equals("")) {
        source.put("_id", s.getId());
      }

      if (s.getName() != null && !s.getName().equals("")) {
        source.put("name", s.getName());
      }

      if (s.getVersion() != null && !s.getVersion().equals("")) {
        source.put("version", s.getVersion());
      }
    }

    return source;
  }

}
