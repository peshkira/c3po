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

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.utils.Configurator;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class MongoPersistenceLayerTest {
    PersistenceLayer pLayer;
    final Logger LOG = LoggerFactory.getLogger(MongoPersistenceLayerTest.class);

    @Before
    public void setUp() throws Exception {
        helpers.DataOps.insertData();
        pLayer = Configurator.getDefaultConfigurator().getPersistence();
    }

    @After
    public void tearDown() throws Exception {
        helpers.DataOps.removeData();
    }

    @Test
    public void shouldTestFind() {
        if (this.pLayer.isConnected()) {
            Iterator<Element> iter = pLayer.find(Element.class, null);
            List<Element> elements = new ArrayList<Element>();
            while (iter.hasNext()) {
                elements.add(iter.next());
            }
            Assert.assertEquals(5, elements.size());
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
            //  this.insertTestData();

            pLayer.remove(Element.class, null);
            Iterator<Element> find = pLayer.find(Element.class, null);

            Assert.assertFalse(find.hasNext());
        }
    }

    @Test
    public void shouldTestRemoveOne() throws Exception {
        if (this.pLayer.isConnected()) {
            //   this.insertTestData();

            Iterator<Element> find = pLayer.find(Element.class, null);
            Element next = find.next();

            this.pLayer.remove(next);

            find = pLayer.find(Element.class, null);

            List<Element> elements = new ArrayList<Element>();
            while (find.hasNext()) {
                elements.add(find.next());
            }

            //     Assert.assertEquals(4, elements.size());
        }
    }

    @Test
    public void shouldTestInsert() throws Exception {
        if (this.pLayer.isConnected()) {

            Iterator<Element> iter = pLayer.find(Element.class, null);
            //  assertFalse(iter.hasNext());

            //  this.insertTestData();

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

            //Assert.assertEquals(8, elements.size());

            Element upadtedElement = new Element("changed", "", "");

            this.pLayer.update(upadtedElement, filter);

            iter = this.pLayer.find(Element.class, filter);

            while (iter.hasNext()) {
                Element e = iter.next();
                Assert.assertEquals("test", e.getCollection());
//        Assert.assertTrue(Arrays.asList("test1", "test2", "test3").contains(e.getUid()));
            }

        }
    }

    @Test
    public void shouldTestNumericAggregation() throws Exception {
        if (this.pLayer.isConnected()) {
            // this.insertTestData();
            Property property = this.pLayer.getCache().getProperty("pagecount");
            // NumericStatistics numericStatistics = this.pLayer.getNumericStatistics(property, new Filter(new FilterCondition(
            //     "collection", "test")));

            //    Assert.assertEquals(3, numericStatistics.getCount());
            //    Assert.assertEquals(42D, numericStatistics.getAverage());
            //    Assert.assertEquals(42D, numericStatistics.getMax());
            //    Assert.assertEquals(42D, numericStatistics.getMin());
            //   Assert.assertEquals(0D, numericStatistics.getStandardDeviation());
            //   Assert.assertEquals(0D, numericStatistics.getVariance());
        }
    }

    @Test
    public void shouldTestHistogramGeneration() throws Exception {

        if (this.pLayer.isConnected()) {
            // this.insertTestData();

            Property mimetype = this.pLayer.getCache().getProperty("mimetype");
            // Map<String, Long> mimetypeHistogram = this.pLayer.getValueHistogramFor(mimetype, null);

            // Assert.assertEquals(2, mimetypeHistogram.keySet().size());

            //Long pdfs = mimetypeHistogram.get("application/pdf");
            // Long htms = mimetypeHistogram.get("text/html");

//      Assert.assertEquals(new Long(2), pdfs);
//      Assert.assertEquals(new Long(1), htms);
        }
    }

    @Test
    public void SuperMapReduceTest() throws Exception {

        //setup();


        Map<String, List<Integer>> binThresholds = new HashMap<String, List<Integer>>();
        List<Integer> bins = new ArrayList<Integer>();
        bins.add(5);
        bins.add(20);
        bins.add(40);
        bins.add(100);
        bins.add(1000);
        bins.add(10000);
        bins.add(10000000);
        binThresholds.put("size", bins);
        binThresholds.put("wordcount", bins);
        binThresholds.put("pagecount", bins);

        List<String> properties = new ArrayList<String>();
        properties.add("mimetype");
        properties.add("format");
        properties.add("wordcount");
        Map<String, Map<String, Long>> stringMapMap = pLayer.getHistograms(properties, null, binThresholds);
        org.junit.Assert.assertEquals(stringMapMap.size(), 3);


    }


    private void insertTestData() {
        Element e1 = new Element("test", "test1", "Some name 1");
        Element e2 = new Element("test", "test2", "Some name 2");
        Element e3 = new Element("test", "test3", "Some name 3");

        Property property = new Property("pagecount", PropertyType.INTEGER);
        Property mimetype = new Property("mimetype");

        MetadataRecord rec = new MetadataRecord(property.getKey(), "42");
        MetadataRecord pdf = new MetadataRecord(mimetype.getKey(), "application/pdf");
        MetadataRecord htm = new MetadataRecord(mimetype.getKey(), "text/html");

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
