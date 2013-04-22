package com.petpet.c3po.dao.mongo;

import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Property;

public class PropertyDeserialzer implements ModelDeserializer {

  //TODO do proper null checks
  // return empty property?
  @Override
  public Property deserialize(Object object) {
    DBObject dbObject = (DBObject) object;
    
    return this.parseProperty(dbObject);
  }

  private Property parseProperty(DBObject obj) {
    Property result = null;
    if (obj != null) {
      String key = (String) obj.get("key");
      String type = (String) obj.get("type");

      result = new Property(key);
      result.setType(type);
    }
    
    return result;
  }
  
}
