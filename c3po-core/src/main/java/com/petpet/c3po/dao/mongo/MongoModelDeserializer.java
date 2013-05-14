package com.petpet.c3po.dao.mongo;

/**
 * Deserializes the given object into a model object.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface MongoModelDeserializer {

  /**
   * Deserializes the given object into the correct Java object.
   * 
   * @param object
   *          the object to deserialize.
   * @return the deserialized object.
   */
  Object deserialize( Object object );

}
