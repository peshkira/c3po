package com.petpet.c3po.dao.mongo;

import com.mongodb.DBObject;

/**
 * A simple interface for serializing objects into Mongo {@link DBObject}s.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface MongoModelSerializer {

  /**
   * Serializes the given object to a {@link DBObject}. It is up to the backend
   * provider how the POJO will be mapped into the mongo schema.
   * 
   * @param object
   *          the object to serialize.
   * @return the serialized {@link DBObject}
   */
  DBObject serialize( Object object );

}
