package com.petpet.c3po.api;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * An interface for a meta data gatherer. The implementing class could be a
 * filesystem gatherer or some specific repository gatherer.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface MetaDataGatherer {

  /**
   * Some gatherer specific configuration.
   * 
   * @param config
   */
  void setConfig(Map<String, Object> config);

  /**
   * The overall count of the meta data records or -1 if unknown.
   * 
   * @return the count of the records.
   */
  long getCount();

  /**
   * The remaining metadata records or -1 if unknonw.
   * 
   * @return the count of the remaining records.
   */
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
  List<InputStream> getNext(int count);

}
