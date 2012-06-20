package com.petpet.c3po.utils;

import static org.junit.Assert.*;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.MetadataRecord;
import com.petpet.c3po.datamodel.Property;

public class DataHelperTest {

  @Test
  public void shouldTestElementParsing() throws Exception {
    Configurator.getDefaultConfigurator().configure();
    final PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    
    final Property property = p.getCache().getProperty("mimetype");
    final Element e = new Element("test_collection", "uid1", "name1");
    e.setId("some id");
    final MetadataRecord mr = new MetadataRecord(property, "application/pdf");
    mr.setStatus(MetadataRecord.Status.OK.name());
    e.setMetadata(Arrays.asList(mr));

    p.insert(Constants.TBL_ELEMENTS, e.getDocument());
    
    BasicDBObject ref = new BasicDBObject("collection", "test_collection");
    DBCursor obj = p.find(Constants.TBL_ELEMENTS, ref);
    DBObject next = obj.next();
    Assert.assertEquals(1, obj.count());

    p.getDB().getCollection(Constants.TBL_ELEMENTS).remove(ref);
    
    Element parsed = DataHelper.parseElement(next, p);
    
    Assert.assertEquals(e.getCollection(), parsed.getCollection());
    Assert.assertEquals(1, e.getMetadata().size());
    Assert.assertEquals(e.getMetadata().get(0).getProperty().getKey(), parsed.getMetadata().get(0).getProperty().getKey());
  }
}
