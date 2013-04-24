package com.petpet.c3po.dao.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.model.ActionLog;

public class ActionLogSerializer implements ModelSerializer {

  @Override
  public DBObject serialize(Object object) {
    BasicDBObject log = null;

    if (object != null && object instanceof ActionLog) {
      ActionLog l = (ActionLog) object;

      log = new BasicDBObject();
      log.put("collection", l.getCollection());
      log.put("action", l.getAction());
      log.put("date", l.getDate());

    }

    return log;

  }

}
