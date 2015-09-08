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
package com.petpet.c3po.dao;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.mongodb.DBObject;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.dao.mongo.MongoFilterSerializer;

/**
 * Tests whether the {@link MongoFilterSerializer} follows the proposed
 * convention in {@link Filter}.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoFilterSerializerTest {

  MongoFilterSerializer ser;

  @Before
  public void setup() {
    ser = new MongoFilterSerializer();
  }

  @Test
  public void shouldTestMongoSimpleFilterSerialization() throws Exception {
    Filter f = Mockito.mock(Filter.class);
    FilterCondition fc = Mockito.mock(FilterCondition.class);

    String property = "mimetype";
    String value = "applciation/pdf";

    Mockito.when(fc.getField()).thenReturn(property);
    Mockito.when(fc.getValue()).thenReturn(value);

    Mockito.when(f.getConditions()).thenReturn(Arrays.asList(fc));

    DBObject filter = ser.serialize(f);

    String expected = "{ \"$and\" : [ { \"metadata.mimetype.value\" : \"applciation/pdf\"}]}";
    Assert.assertEquals(expected, filter.toString());
  }

  @Test
  public void shouldTestMongoOrFilterSerialization() throws Exception {
    Filter f = Mockito.mock(Filter.class);
    FilterCondition fc1 = Mockito.mock(FilterCondition.class);
    FilterCondition fc2 = Mockito.mock(FilterCondition.class);
    FilterCondition fc3 = Mockito.mock(FilterCondition.class);

    String property = "mimetype";
    String value1 = "applciation/pdf";
    String value2 = "text/html";
    String value3 = "text/xml";

    Mockito.when(fc1.getField()).thenReturn(property);
    Mockito.when(fc1.getValue()).thenReturn(value1);

    Mockito.when(fc2.getField()).thenReturn(property);
    Mockito.when(fc2.getValue()).thenReturn(value2);

    Mockito.when(fc3.getField()).thenReturn(property);
    Mockito.when(fc3.getValue()).thenReturn(value3);

    Mockito.when(f.getConditions()).thenReturn(Arrays.asList(fc1, fc2, fc3));

    DBObject filter = ser.serialize(f);

    String val = filter.toString();
    String expr = "{ \"$and\" : [ { \"$or\" : [ { \"metadata.mimetype.value\" : \"applciation/pdf\"} , { \"metadata.mimetype.value\" : \"text/html\"} , { \"metadata.mimetype.value\" : \"text/xml\"}]}]}";
    Assert.assertEquals(expr, val);
  }

  @Test
  public void shouldTestMongoAndOrFilterSerialization() throws Exception {
    Filter f = Mockito.mock(Filter.class);
    FilterCondition fc1 = Mockito.mock(FilterCondition.class);
    FilterCondition fc2 = Mockito.mock(FilterCondition.class);
    FilterCondition fc3 = Mockito.mock(FilterCondition.class);
    FilterCondition fc4 = Mockito.mock(FilterCondition.class);

    String property1 = "mimetype";
    String property2 = "puid";
    String value1 = "applciation/pdf";
    String value2 = "text/html";
    String value3 = "text/xml";
    String value4 = "fmt/42";

    Mockito.when(fc1.getField()).thenReturn(property1);
    Mockito.when(fc1.getValue()).thenReturn(value1);

    Mockito.when(fc2.getField()).thenReturn(property1);
    Mockito.when(fc2.getValue()).thenReturn(value2);

    Mockito.when(fc3.getField()).thenReturn(property1);
    Mockito.when(fc3.getValue()).thenReturn(value3);

    Mockito.when(fc4.getField()).thenReturn(property2);
    Mockito.when(fc4.getValue()).thenReturn(value4);

    Mockito.when(f.getConditions()).thenReturn(Arrays.asList(fc1, fc2, fc3, fc4));

    DBObject filter = ser.serialize(f);

    String val = filter.toString();
    String expr = "{ \"$and\" : [ { \"metadata.puid.value\" : \"fmt/42\"} , { \"$or\" : [ { \"metadata.mimetype.value\" : \"applciation/pdf\"} , { \"metadata.mimetype.value\" : \"text/html\"} , { \"metadata.mimetype.value\" : \"text/xml\"}]}]}";
     //       Assert.assertEquals(expr, val);
  }

}
