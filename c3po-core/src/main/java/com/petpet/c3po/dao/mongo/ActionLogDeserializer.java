package com.petpet.c3po.dao.mongo;

import java.util.Date;

import com.mongodb.DBObject;
import com.petpet.c3po.api.model.ActionLog;

public class ActionLogDeserializer implements ModelDeserializer {

  // TODO null checks and type check.
  @Override
  public ActionLog deserialize(Object object) {
    DBObject dbObject = (DBObject) object;

    return this.parseActionLog(dbObject);
  }

  private ActionLog parseActionLog(DBObject object) {
    String c = (String) object.get("collection");
    String a = (String) object.get("action");
    Date d = (Date) object.get("date");

    return new ActionLog(c, a, d);
  }

}
