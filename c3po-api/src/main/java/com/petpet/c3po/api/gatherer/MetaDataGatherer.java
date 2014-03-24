/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.petpet.c3po.api.gatherer;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

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

   LinkedBlockingQueue<MetadataStream> getQueue();


  /**
   * Whether or not the gatherer has finished its work.
   * 
   * @return
   */
  boolean isReady();

}
