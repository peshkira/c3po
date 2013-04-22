package com.petpet.c3po.dao.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Source;

public class SourceSerializer implements ModelSerializer {

  @Override
  public DBObject serialize(Object object) {
    BasicDBObject source = null;

    if (object != null && object instanceof Source) {
      Source s = (Source) object;
      source = new BasicDBObject();

      source.put("_id", s.getId());
      source.put("name", s.getName());
      source.put("version", s.getVersion());
    }
    
    return source;
  }

}
