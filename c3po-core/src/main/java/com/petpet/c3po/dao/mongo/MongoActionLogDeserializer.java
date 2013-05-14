package com.petpet.c3po.dao.mongo;

import java.util.Date;

import com.mongodb.DBObject;
import com.petpet.c3po.api.model.ActionLog;

/**
 * Deserializes {@link DBObject}s to {@link ActionLog} objects.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoActionLogDeserializer implements MongoModelDeserializer {

  /**
   * Deserializes the {@link DBObject} to an {@link ActionLog}. Note, that if
   * the passed object is null or not a {@link DBObject} null is returned.
   */
  @Override
  public ActionLog deserialize( Object object ) {
    if ( object == null || !(object instanceof DBObject) ) {
      return null;
    }

    DBObject dbObject = (DBObject) object;

    return this.parseActionLog( dbObject );
  }

  /**
   * Parses the db object to an action log.
   * 
   * @param object
   *          the object to parse.
   * @return the action log.
   */
  private ActionLog parseActionLog( DBObject object ) {
    String c = (String) object.get( "collection" );
    String a = (String) object.get( "action" );
    Date d = (Date) object.get( "date" );

    return new ActionLog( c, a, d );
  }

}
