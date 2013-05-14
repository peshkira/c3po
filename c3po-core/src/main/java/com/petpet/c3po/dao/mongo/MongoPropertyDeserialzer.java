package com.petpet.c3po.dao.mongo;

import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Property;

/**
 * Deserializes the mongo DBObject into a Property object.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoPropertyDeserialzer implements MongoModelDeserializer {

  /**
   * Deserializes the given {@link DBObject} into a property.
   */
  @Override
  public Property deserialize( Object object ) {
    if ( object == null || !(object instanceof DBObject) ) {
      return null;
    }

    DBObject dbObject = (DBObject) object;

    return this.parseProperty( dbObject );
  }

  /**
   * Parses the property.
   * 
   * @param obj
   * @return
   */
  private Property parseProperty( DBObject obj ) {
    Property result = null;
    if ( obj != null ) {
      String key = (String) obj.get( "key" );
      String type = (String) obj.get( "type" );

      result = new Property( key );
      result.setType( type );
    }

    return result;
  }

}
