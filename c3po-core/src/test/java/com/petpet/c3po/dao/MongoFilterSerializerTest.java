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

import java.util.*;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.helper.filtering.PropertyFilterCondition;
import com.petpet.c3po.utils.Configurator;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.mongodb.DBObject;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.dao.mongo.MongoFilterSerializer;

/**
 * Tests whether the {@link MongoFilterSerializer} follows the proposed
 * convention in {@link filtering}.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MongoFilterSerializerTest {

  MongoFilterSerializer ser;
  PersistenceLayer pLayer;

  @Before
  public void setUp() throws Exception {
    System.out.println("Setting up the test. Inserting the data");

    ser=new MongoFilterSerializer();
  }

  @After
  public void tearDown() throws Exception {
    System.out.println("Tearing down the test. Removing the data");
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

    String expected = "{ \"$and\" : [ { \"mimetype.values\" : \"applciation/pdf\"}]}";
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
    String expr = "{ \"$and\" : [ { \"$or\" : [ { \"mimetype.values\" : \"applciation/pdf\"} , { \"mimetype.values\" : \"text/html\"} , { \"mimetype.values\" : \"text/xml\"}]}]}";
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
    String expr = "{ \"$and\" : [ { \"puid.value\" : \"fmt/42\"} , { \"$or\" : [ { \"mimetype.value\" : \"applciation/pdf\"} , { \"mimetype.value\" : \"text/html\"} , { \"mimetype.value\" : \"text/xml\"}]}]}";
     //       Assert.assertEquals(expr, val);
  }


  @Test
  public void ShouldTestSerialiseNew() throws Exception {
    Filter f=new Filter();
    List<PropertyFilterCondition> pfcs=new ArrayList<PropertyFilterCondition>();

    PropertyFilterCondition pfc1=new PropertyFilterCondition();
    pfc1.setProperty("format");
    List<String> statuses=new ArrayList<String>();
    statuses.add("CONFLICT");
    statuses.add("OK");
    pfc1.setStatuses(statuses);
    List<String> values=new ArrayList<String>();
    values.add("Hypertext Markup Language");
    pfc1.setValues(values);

    Map<String, String> sourcedvalues=new HashMap<String, String>();

    sourcedvalues.put("ffident:0.2", "Hypertext Markup Language");


    pfc1.setSourcedValues(sourcedvalues);


    pfcs.add(pfc1);

   // Assert.assertEquals("{ \"$and\" : [ { \"metadata.sourcedValues\" : { \"$elemMatch\" : { \"source\" : \"31\" , \"value\" : \"Hypertext Markup Language\"}}} , { \"metadata.property\" : \"format\"} , { \"$or\" : [ { \"metadata.status\" : \"CONFLICT\"} , { \"metadata.status\" : \"OK\"}]} , { \"$and\" : [ { \"metadata.sourcedValues.value\" : \"Hypertext Markup Language\"}]}]}",s);

  }

}
