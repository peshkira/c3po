package com.petpet.c3po.api.model.helper;

/**
 * This interface abstracts a gathered resource that is passed for parsing to
 * the adaptors.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface MetadataStream {

  /**
   * The name of the resource (e.g. a file name or some uri)
   * 
   * @return the resource name.
   */
  String getName();

  /**
   * The data that has to be parsed. The implementing class should read in the
   * data when this method is called - when possible.
   * 
   * @return the data to be parse, e.g. the content of the resource.
   */
  String getData();

}
