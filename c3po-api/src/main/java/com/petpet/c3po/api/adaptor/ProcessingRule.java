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

/**
 * A processing rule is a rule that has a priority.
 * 
 * @see PreProcessingRule
 * @see PostProcessingRule
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface ProcessingRule {

  /**
   * A priority between 0 and 1000 in the case of more rules, where 0 is the
   * least important and 1000 is the most important rule. If the set priority is
   * smaller than 0, then 0 will be used, if it is larger than 1000, then 1000
   * will be used.
   * 
   * @return the priority of this rule.
   */
  int getPriority();
}
