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

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import com.petpet.c3po.api.model.Property;

public class HtmlInfoProcessingRuleTest {

  @Test
  public void shouldTestFaultyTagOccurrences() throws Exception {
    Property p1 = Mockito.mock(Property.class);
    Property p2 = Mockito.mock(Property.class);

    Mockito.when(p1.getKey()).thenReturn("bodyTagOccurences");
    Mockito.when(p2.getKey()).thenReturn("asdasdTagOccurences");

    HtmlInfoProcessingRule rule = new HtmlInfoProcessingRule();
    boolean skipOnValid = rule.shouldSkip(p1.getKey(), "whatever", "whatever", "HtmlInfo", "whatever");
    boolean skipOnFaulty = rule.shouldSkip(p2.getKey(), "whatever", "whatever", "HtmlInfo", "whatever");

    Assert.assertFalse(skipOnValid);
    Assert.assertTrue(skipOnFaulty);

  }
}
