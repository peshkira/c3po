package com.petpet.c3po.dao.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.model.ActionLog;

/**
 * Serializes an {@link ActionLog} object to a mongo {@link DBObject}.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoActionLogSerializer implements MongoModelSerializer {

  /**
   * Maps the given {@link ActionLog} to a mongo document. Note that if the
   * given object is null or not of type {@link ActionLog}, then null is
   * returned.
   */
  @Override
  public DBObject serialize(Object object) {
    BasicDBObject log = null;

    if (object != null && object instanceof ActionLog) {
      ActionLog l = (ActionLog) object;

      log = new BasicDBObject();

      if (l.getCollection() != null && !l.getCollection().equals("")) {
        log.put("collection", l.getCollection());
      }

      if (l.getAction() != null && !l.getAction().equals("")) {
        log.put("action", l.getAction());
      }

      if (l.getDate() != null && !l.getDate().equals("")) {
        log.put("date", l.getDate());
      }

    }

    return log;

  }

}
