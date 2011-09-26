package com.petpet.collpro.api;

import com.petpet.collpro.api.utils.ConfigurationException;

import java.util.Map;

public interface ITool extends Observable {

  /**
   * Starts the tool. This method should be called after configure.
   */
  void execute();

  /**
   * Sets the configuration parameters of the tool. For instance the collection
   * to on which the gatherer will work, the file path, eventually server
   * config, etc.
   * 
   * @param configuration
   *          the configuration map.
   */
  void configure(Map<String, Object> configuration) throws ConfigurationException;

}
