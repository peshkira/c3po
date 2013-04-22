package com.petpet.c3po.dao.mongo;

import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Source;

public class SourceDeserializer implements ModelDeserializer {

  // TODO do null checks
  @Override
  public Object deserialize(Object object) {
    DBObject dbObject = (DBObject) object;

    String id = (String) dbObject.get("_id");
    String name = (String) dbObject.get("name");
    String version = (String) dbObject.get("version");

    Source s = new Source();
    s.setId(id);
    s.setName(name);
    s.setVersion(version);

    return s;
  }

}
