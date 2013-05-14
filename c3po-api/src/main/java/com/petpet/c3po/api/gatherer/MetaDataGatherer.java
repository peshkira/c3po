package com.petpet.c3po.api.gatherer;

import java.util.Map;

import com.petpet.c3po.api.model.helper.MetadataStream;

/**
 * An interface for a meta data gatherer. The implementing class could be a
 * filesystem gatherer or some specific repository gatherer.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface MetaDataGatherer extends Runnable {

  /**
   * Some gatherer specific configuration.
   * 
   * @param config
   */
  void setConfig( Map<String, String> config );

  /**
   * Gets the next {@link MetadataStream} object.
   * 
   * @return
   */
  MetadataStream getNext();

  /**
   * Whether or not the gatherer has a next element at the moment of the call.
   * 
   * @return
   */
  boolean hasNext();

  /**
   * Whether or not the gatherer has finished its work.
   * 
   * @return
   */
  boolean isReady();

}
