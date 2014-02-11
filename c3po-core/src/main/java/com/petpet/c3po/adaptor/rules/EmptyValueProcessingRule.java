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

import com.petpet.c3po.api.adaptor.PreProcessingRule;

/**
 * A {@link PreProcessingRule} that skips the meta data records if their value
 * is null or an empty string. Note that this rule is turned on per default and
 * can be disabled via the .c3poconfig file with the following key set to false:
 * 'c3po.rule.empty_value_processing'.
 * 
 * Note that it is up to the adaptor implementation, whether or not it will
 * apply this rule.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class EmptyValueProcessingRule implements PreProcessingRule {

  /**
   * Skips the empty values.
   */
  @Override
  public boolean shouldSkip( String property, String value, String status, String tool, String version ) {
    if ( value == null || value.equals( "" ) ) {
      return true;
    }
    return false;
  }

  /**
   * Has a high priority.
   */
  @Override
  public int getPriority() {
    return 990;
  }

  @Override
  public void onCommandFinished() {
    // do nothing
  }
}
