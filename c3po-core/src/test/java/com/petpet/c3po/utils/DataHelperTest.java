package com.petpet.c3po.utils;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.common.Constants;

public class DataHelperTest {

  @Test
  public void shouldTestElementParsing() throws Exception {
    Configurator.getDefaultConfigurator().configure();
    final PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    
    final Property property = p.getCache().getProperty("mimetype");
    Source source = p.getCache().getSource("Jhove", "1.5");
    Source source2 = p.getCache().getSource("ffident", "0.2");
    final Element e = new Element("test_collection", "uid1", "name1");
    e.setId("some id");
    final MetadataRecord mr = new MetadataRecord(property, "application/pdf");
    mr.setStatus(MetadataRecord.Status.OK.name());
    mr.setSources(Arrays.asList(source.getId(), source2.getId()));
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
    Assert.assertEquals(source.getName()+" " +source.getVersion(), parsed.getMetadata().get(0).getSources().get(0));
  }
  
}
