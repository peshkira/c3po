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
package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;

/**
 * This {@link PostProcessingRule} applies the given collection to every
 * {@link Element} that is processed.
 * 
 * Note that this rule is always turned on.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class AssignCollectionToElementRule implements PostProcessingRule {

  /**
   * The collection to apply.
   */
  private String collectionName;

  /**
   * Creates the rule.
   * 
   * @param name
   *          the name of the collection.
   */
  public AssignCollectionToElementRule(String name) {
    this.collectionName = name;
  }

  /**
   * Has a very high priority.
   */
  @Override
  public int getPriority() {
    return 990;
  }

  @Override
  public void onCommandFinished() {
    // do nothing
  }

  /**
   * Sets the collection of the given element to the colleciton name and returns
   * it.
   */
  @Override
  public Element process( Element e ) {
    if ( e != null ) {
      e.setCollection( this.collectionName );
    }

    return e;
  }

}
