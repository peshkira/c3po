package com.petpet.c3po.dao.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Property;

public class PropertySerializer implements ModelSerializer {

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
