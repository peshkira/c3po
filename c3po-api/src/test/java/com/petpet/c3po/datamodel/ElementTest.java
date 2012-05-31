package com.petpet.c3po.datamodel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.petpet.c3po.datamodel.MetadataRecord.Status;
import com.petpet.c3po.datamodel.Property.PropertyType;

public class ElementTest {

  @Test
  public void shouldTestElementDocumentCreation() throws Exception {
    String collection = "test";
    String uid = "testuid";
    String name = "testname";
    String key1 = "pkey1";
    String key2 = "pkey2";
    Element e = new Element(collection, uid, name);

    Property p1 = new Property(key1, key1);
    Property p2 = new Property(key2, key2);

    MetadataRecord r1 = new MetadataRecord(p1, "42");
    MetadataRecord r2 = new MetadataRecord(p2, "21");

    e.setMetadata(Arrays.asList(r1, r2));

    BasicDBObject document = e.getDocument();

    Assert.assertEquals(uid, document.get("uid"));
    Assert.assertEquals(name, document.get("name"));
    Assert.assertEquals(collection, document.get("collection"));

    BasicDBObject meta = (BasicDBObject) document.get("metadata");
    Assert.assertNotNull(meta);
    Assert.assertEquals(2, meta.keySet().size());

    Assert.assertTrue(meta.containsField(p1.getId()));
    Assert.assertTrue(meta.containsField(p2.getId()));

  }
  
  @Test
  public void shouldTestElementDocumentCreationWithConflictedMetadata() throws Exception {
    String collection = "test";
    String uid = "testuid";
    String name = "testname";
    String key1 = "pkey1";
    String key2 = "pkey2";
    Element e = new Element(collection, uid, name);

    Property p1 = new Property(key1, key1);
    Source s1 = new Source("tool", "v0.1");
    Source s2 = new Source("tool", "v0.2");

    MetadataRecord r1 = new MetadataRecord(p1, "42");
    MetadataRecord r2 = new MetadataRecord(p1, "21");
    r1.setStatus(Status.CONFLICT.name());
    r1.setSources(Arrays.asList(s1.getId()));
    r2.setStatus(Status.CONFLICT.name());
    r2.setSources(Arrays.asList(s2.getId()));

    e.setMetadata(Arrays.asList(r1, r2));

    BasicDBObject document = e.getDocument();

    Assert.assertEquals(uid, document.get("uid"));
    Assert.assertEquals(name, document.get("name"));
    Assert.assertEquals(collection, document.get("collection"));

    BasicDBObject meta = (BasicDBObject) document.get("metadata");
    Assert.assertNotNull(meta);
    Assert.assertEquals(1, meta.keySet().size());

    Assert.assertTrue(meta.containsField(p1.getId()));

    BasicDBObject value = (BasicDBObject) meta.get(p1.getId());
    Assert.assertNotNull(value);
    
    Assert.assertNull(value.get("value"));
    List<Object> values = (List<Object>) value.get("values");
    Assert.assertNotNull(values);
    Assert.assertEquals(2, values.size());
  }

  @Test
  public void shouldTestTypedValueRetrievalForBoolean() throws Exception {
    Element test = new Element("test", "me");
    Object res = test.getTypedValue(PropertyType.BOOL.name(), "yEs");

    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Boolean);

    res = test.getTypedValue(PropertyType.BOOL.name(), "nO");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Boolean);

    res = test.getTypedValue(PropertyType.BOOL.name(), "tRuE");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Boolean);

    res = test.getTypedValue(PropertyType.BOOL.name(), "FalSe");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Boolean);

    res = test.getTypedValue(PropertyType.BOOL.name(), "abc");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof String);

    res = test.getTypedValue(PropertyType.BOOL.name(), "1");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof String);
  }

  @Test
  public void shouldTestTypedValueRetrievalForInteger() throws Exception {
    Element test = new Element("test", "me");
    Object res = test.getTypedValue(PropertyType.INTEGER.name(), "42");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Long);

    res = test.getTypedValue(PropertyType.INTEGER.name(), Integer.MAX_VALUE + "");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Long);

    res = test.getTypedValue(PropertyType.INTEGER.name(), Integer.MIN_VALUE + "");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Long);

    res = test.getTypedValue(PropertyType.INTEGER.name(), Long.MAX_VALUE + "");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Long);

    res = test.getTypedValue(PropertyType.INTEGER.name(), "abc");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof String);

  }
  
  @Test
  public void shouldTestTypedValueRetrievalForFloat() throws Exception {
    Element test = new Element("test", "me");
    Object res = test.getTypedValue(PropertyType.FLOAT.name(), "42");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Double);

    res = test.getTypedValue(PropertyType.FLOAT.name(), "42.0");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Double);

    res = test.getTypedValue(PropertyType.FLOAT.name(), Double.MAX_VALUE + "");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Double);

    res = test.getTypedValue(PropertyType.FLOAT.name(), "abc");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof String);
    
  }
  
  @Test
  public void shouldTestTypedValueRetrivalForDate() throws Exception {
    Element test = new Element("test", "me");
    Object res = test.getTypedValue(PropertyType.DATE.name(), "20121221122121");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Date);
    
    res = test.getTypedValue(PropertyType.DATE.name(), "1338474281528");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof Date);
    
    res = test.getTypedValue(PropertyType.DATE.name(), "blah");
    Assert.assertNotNull(res);
    Assert.assertTrue(res instanceof String);
    
  }

}
