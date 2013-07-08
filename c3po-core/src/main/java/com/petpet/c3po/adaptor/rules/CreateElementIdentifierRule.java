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

import java.util.UUID;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.utils.Configurator;

/**
 * A {@link PostProcessingRule} that creates an element uid if it was not
 * created by adaptor for some reason. This rule is turned on per default and
 * can be disabled via the .c3poconfig file with the following key set to false:
 * 'c3po.rule.create_element_identifier'
 * 
 * @see Configurator
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class CreateElementIdentifierRule implements PostProcessingRule {

  /**
   * This rule has a low priority.
   */
  @Override
  public int getPriority() {
    return 10;
  }

  @Override
  public void onCommandFinished() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   * Sets a random uuid if the element has no uid defined.
   */
  @Override
  public Element process( Element e ) {
    if ( e != null && e.getUid() == null ) {
      e.setUid( UUID.randomUUID().toString() );
    }

    return e;
  }

}
