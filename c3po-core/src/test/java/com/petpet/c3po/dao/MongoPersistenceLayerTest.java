package com.petpet.c3po.dao;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    config.put("db.name", "mongo_test");

    DataHelper.init();

    try {
      pLayer.establishConnection(config);

    } catch (C3POPersistenceException e) {
      LOG.warn("Could not establish a connection to the persistence layer. All tests will be skipped");
    }
  }

  @After
  public void tearDown() {
    if (this.pLayer.isConnected()) {
      this.pLayer.clearCache();
      this.pLayer.remove(Element.class, null);
      this.pLayer.remove(Property.class, null);
      try {
        this.pLayer.close();
      } catch (C3POPersistenceException e) {
        LOG.warn("Could not close the connection in a clear fashion");
      }
    }
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
