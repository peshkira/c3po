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
package com.petpet.c3po.analysis;

import java.util.List;
import java.util.Map;

import com.petpet.c3po.api.model.helper.Filter;

/**
 * A representative generator is a class that can select a set of sample objects
 * that are somehow representative to a collection or a super set of elements.
 * There are different implementation strategies for finding representative
 * samples.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public abstract class RepresentativeGenerator {

  /**
   * A filter used to determine the set on which the representative algorithm is
   * going to be used. Note, that the filter can only specify the collection,
   * meaning that all data should be considered.
   */
  public Filter filter;

  /**
   * A simple map to hold the configuration of the representative generator if
   * any is needed at all.
   */
  private Map<String, Object> options;

  /**
   * Retrieves a list of identifiers for the representative sample objects. It
   * is the responsibility of the method to decide how many objects should be
   * returned. Usually a small number of up to 10 is good for planning
   * experiments.
   *
   * @return the list of sample object identifiers.
   */
  public abstract List<String> execute();

  /**
   * Retrieves a list of identifiers for the representative sample objects with
   * a predefined number of objects. If the limit is bigger then the set on
   * which the class operates, then all objects should be returned. Note that
   * the method implementation might return less sample records than the
   * specified limit.
   * 
   * @param limit
   *          a max number of sample records to be returned by this method.
   * @return the list of sample object identifiers.
   */
  public abstract List<String> execute( int limit );

  /**
   * Retrieves the type/name of the algorithm used to identify the sample
   * objects.
   * 
   * @return the type.
   */
  public abstract String getType();

  public Filter getFilter() {
    return filter;
  }

  public void setFilter( Filter filter ) {
    this.filter = filter;
  }

  public Map<String, Object> getOptions() {
    return options;
  }

  public void setOptions( Map<String, Object> options ) {
    this.options = options;
  }

}
