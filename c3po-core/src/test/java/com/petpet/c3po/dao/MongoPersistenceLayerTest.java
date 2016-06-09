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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mongodb.DBObject;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.utils.DataHelper;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

public class MongoPersistenceLayerTest {

  private static final Logger LOG = LoggerFactory.getLogger(MongoPersistenceLayerTest.class);

  MongoPersistenceLayer pLayer;

  @Before
  public void setup() {
    pLayer = new MongoPersistenceLayer();

    Map<String, String> config = new HashMap<String, String>();
    config.put("db.host", "localhost");
    config.put("db.port", "27017");
    config.put("db.name", "c3po");

    DataHelper.init();

    try {
      pLayer.establishConnection(config);

    } catch (C3POPersistenceException e) {
      LOG.warn("Could not establish a connection to the persistence layer. All tests will be skipped");
    }
  }

  @After
  public void tearDown() {
   // if (this.pLayer.isConnected()) {
   //   this.pLayer.clearCache();
   //   this.pLayer.remove(Element.class, null);
  //    this.pLayer.remove(Property.class, null);
   //   try {
   //     this.pLayer.close();
   //   } catch (C3POPersistenceException e) {
    //    LOG.warn("Could not close the connection in a clear fashion");
   //   }
   // }
  }

  @Test
  public void shouldTestFind() {
    if (this.pLayer.isConnected()) {

      this.insertTestData();

      Iterator<Element> iter = pLayer.find(Element.class, null);
      List<Element> elements = new ArrayList<Element>();

      while (iter.hasNext()) {
        elements.add(iter.next());
      }

      Assert.assertEquals(3, elements.size());
      Element element = elements.get(0);
      Assert.assertTrue(Arrays.asList("test1", "test2", "test3").contains(element.getUid()));
    }
  }

  @Test
  public void shouldTestFindOne() throws Exception {
    if (this.pLayer.isConnected()) {

      this.insertTestData();

      Iterator<Element> find = pLayer.find(Element.class, new Filter(new FilterCondition("uid", "test1")));

      Assert.assertTrue(find.hasNext());

      Element next = find.next();
      Assert.assertEquals("test1", next.getUid());

      Assert.assertFalse(find.hasNext());
    }
  }

  @Test
  public void shouldTestRemoveAll() throws Exception {
    if (this.pLayer.isConnected()) {
      this.insertTestData();

      pLayer.remove(Element.class, null);
      Iterator<Element> find = pLayer.find(Element.class, null);

      Assert.assertFalse(find.hasNext());
    }
  }

  @Test
  public void shouldTestRemoveOne() throws Exception {
    if (this.pLayer.isConnected()) {
      this.insertTestData();

      Iterator<Element> find = pLayer.find(Element.class, null);
      Element next = find.next();

      this.pLayer.remove(next);

      find = pLayer.find(Element.class, null);

      List<Element> elements = new ArrayList<Element>();
      while (find.hasNext()) {
        elements.add(find.next());
      }

      Assert.assertEquals(2, elements.size());
    }
  }

  @Test
  public void shouldTestInsert() throws Exception {
    if (this.pLayer.isConnected()) {

      Iterator<Element> iter = pLayer.find(Element.class, null);
      assertFalse(iter.hasNext());

      this.insertTestData();

      iter = pLayer.find(Element.class, null);
      assertTrue(iter.hasNext());
    }
  }

  @Test
  public void shouldTestUpdate() throws Exception {
    if (this.pLayer.isConnected()) {
      this.insertTestData();

      Filter element1 = new Filter(new FilterCondition("uid", "test1"));
      Iterator<Element> iter = this.pLayer.find(Element.class, element1);
      Assert.assertTrue(iter.hasNext());

      Element e = iter.next();
      Assert.assertEquals("Some name 1", e.getName());
      String updated = "Updated Name";
      
      e.setName(updated);
      
      this.pLayer.update(e, element1);
      
      iter = this.pLayer.find(Element.class, element1);
      Assert.assertTrue(iter.hasNext());
      e = iter.next();
      
      Assert.assertEquals(updated, e.getName());
    }
  }
  
  @Test
  public void shouldTestUpdateAll() throws Exception {
    if (this.pLayer.isConnected()) {
      this.insertTestData();

      Filter filter = new Filter(new FilterCondition("collection", "test"));
      List<Element> elements = new ArrayList<Element>();
      Iterator<Element> iter = this.pLayer.find(Element.class, filter);
      
      while (iter.hasNext()) {
        elements.add(iter.next());
      }

      Assert.assertEquals(3, elements.size());

      Element upadtedElement = new Element("changed", "", "");
      
      this.pLayer.update(upadtedElement, filter);
      
      iter = this.pLayer.find(Element.class, filter);
      
      while (iter.hasNext()) {
        Element e = iter.next();
        Assert.assertEquals("changed", e.getCollection());
        Assert.assertTrue(Arrays.asList("test1", "test2", "test3").contains(e.getUid()));
      }
      
    }
  }

  @Test
  public void shouldTestNumericAggregation() throws Exception {
    if (this.pLayer.isConnected()) {
      this.insertTestData();
      Property property = this.pLayer.getCache().getProperty("pagecount");
      NumericStatistics numericStatistics = this.pLayer.getNumericStatistics(property, new Filter(new FilterCondition(
          "collection", "test")));

      Assert.assertEquals(3, numericStatistics.getCount());
      Assert.assertEquals(42D, numericStatistics.getAverage());
      Assert.assertEquals(42D, numericStatistics.getMax());
      Assert.assertEquals(42D, numericStatistics.getMin());
      Assert.assertEquals(0D, numericStatistics.getStandardDeviation());
      Assert.assertEquals(0D, numericStatistics.getVariance());
    }
  }

  @Test
  public void shouldTestHistogramGeneration() throws Exception {

    if (this.pLayer.isConnected()) {
      this.insertTestData();

      Property mimetype = this.pLayer.getCache().getProperty("mimetype");
      Map<String, Long> mimetypeHistogram = this.pLayer.getValueHistogramFor(mimetype, null);

      Assert.assertEquals(2, mimetypeHistogram.keySet().size());

      Long pdfs = mimetypeHistogram.get("application/pdf");
      Long htms = mimetypeHistogram.get("text/html");

      Assert.assertEquals(new Long(2), pdfs);
      Assert.assertEquals(new Long(1), htms);
    }
  }

  @Test
  public void SuperMapReduceTest() throws Exception {

    setup();

      List<String> properties=new ArrayList<String>();
      properties.add("mimetype");
      properties.add("format");
    properties.add("wordcount");
    DBObject dbObject = pLayer.mapReduce(0, properties, null);



  }



  private void insertTestData() {
    Element e1 = new Element("test", "test1", "Some name 1");
    Element e2 = new Element("test", "test2", "Some name 2");
    Element e3 = new Element("test", "test3", "Some name 3");
    
    Property property = new Property("pagecount", PropertyType.INTEGER);
    Property mimetype = new Property("mimetype");

    MetadataRecord rec = new MetadataRecord(property, "42");
    MetadataRecord pdf = new MetadataRecord(mimetype, "application/pdf");
    MetadataRecord htm = new MetadataRecord(mimetype, "text/html");

    e1.setMetadata(Arrays.asList(rec, pdf));
    e2.setMetadata(Arrays.asList(rec, pdf));
    e3.setMetadata(Arrays.asList(rec, htm));

    this.pLayer.insert(property);
    this.pLayer.insert(mimetype);
    this.pLayer.insert(e1);
    this.pLayer.insert(e2);
    this.pLayer.insert(e3);
  }
}
