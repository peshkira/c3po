package com.petpet.c3po.dao.mongo;

import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Source;

public class MongoSourceDeserializer implements MongoModelDeserializer {

  /**
   * Deserializes a {@link DBObject} into a {@link Source} object.
   */
  @Override
  public Object deserialize( Object object ) {

    if ( object == null || !(object instanceof DBObject) ) {
      return null;
    }
    DBObject dbObject = (DBObject) object;

    String id = (String) dbObject.get( "_id" );
    String name = (String) dbObject.get( "name" );
    String version = (String) dbObject.get( "version" );

    Source s = new Source();
    s.setId( id );
    s.setName( name );
    s.setVersion( version );

    return s;
  }

}
