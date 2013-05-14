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
package com.petpet.c3po.api.adaptor;

import com.petpet.c3po.api.model.Element;

/**
 * A post processing rule gets a parsed element and does some processing on the
 * given element. It can modify the element as it sees fit. All active post
 * processing rules are applied after the parsing according to their respective
 * priority.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface PostProcessingRule extends ProcessingRule {

  /**
   * This method does some processing over the passed element and returns it. It
   * can do any kind of post processing to the element.
   * 
   * @param e
   *          the element to process.
   * @return the processed element.
   */
  Element process( Element e );

}
