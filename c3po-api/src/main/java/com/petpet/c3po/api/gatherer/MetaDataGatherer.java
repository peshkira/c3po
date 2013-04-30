package com.petpet.c3po.api.gatherer;

import java.util.List;
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
  void setConfig(Map<String, String> config);

  /**
   * The overall count of the meta data records or -1 if unknown.
   * 
   * @return the count of the records.
   */
  @Deprecated
  long getCount();

  /**
   * The remaining metadata records or -1 if unknonw.
   * 
   * @return the count of the remaining records.
   */
  @Deprecated
  long getRemaining();

  /**
   * A list of input streams (of the meta data) for the next N records. If the
   * count is less than 0 then an empty list is returned. If the count is larger
   * than the remaining records then all remaining records are returned.
   * 
   * @param count
   *          the desired number of records.
   * @return
   */
  List<MetadataStream> getNext(int count);

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
