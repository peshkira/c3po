package com.petpet.c3po.datamodel;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.RollbackException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.petpet.c3po.LocalPersistenceLayer;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;

public class ValueTest {

  private static PersistenceLayer pl;
  private static Element edummy;
  private static Property pdummy;
  private static ValueSource vdummy;

  @BeforeClass
  public static void beforeTests() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("LocalC3POPersistenceUnit");
    pl = new LocalPersistenceLayer(emf);

    DigitalCollection coll = new DigitalCollection("Test");
    edummy = new Element("Dummy", "Dummy");
    pdummy = new Property("Dummy");
    vdummy = new ValueSource("Dummy");

    edummy.setCollection(coll);
    edummy.setCollection(coll);
    edummy.setCollection(coll);

    pl.handleCreate(DigitalCollection.class, coll);
    pl.handleCreate(Element.class, edummy);
    pl.handleCreate(Property.class, pdummy);
    pl.handleCreate(ValueSource.class, vdummy);
  }

  @After
  public void after() {
    pl.getEntityManager().clear();
  }

  @Test
  public void shouldStoreStringValue() throws Exception {
    String name = "Test";
    Date date = new Date();

    Value v = new StringValue();
    v.setMeasuredAt(date.getTime());
    v.setReliability(100);
    v.setTypedValue(name);
    v.setElement(edummy);
    v.setProperty(pdummy);
    v.setSource(vdummy);

    pl.handleCreate(Value.class, v);
    pl.getEntityManager().clear();

    Value value = pl.getEntityManager().find(Value.class, 1L);
    Assert.assertNotNull(value);
    Assert.assertEquals(date.getTime(), value.getMeasuredAt());
    Assert.assertEquals(100, value.getReliability());
  }

  @Test
  public void shouldUpdateValue() throws Exception {
    String updated = "updated";

    Value value = pl.getEntityManager().find(Value.class, 1L);
    value.setTypedValue(updated);
    pl.handleCreate(Value.class, value);
    pl.getEntityManager().clear();

    value = pl.getEntityManager().find(Value.class, 1L);
    Assert.assertEquals(updated, value.getTypedValue());
  }

  @Test
  public void shouldDeleteValue() throws Exception {

    Value value = pl.getEntityManager().find(Value.class, 1L);
    Assert.assertNotNull(value);
    value.getElement().getValues().remove(value);
    pl.handleDelete(Value.class, value);
    pl.getEntityManager().clear();

    value = pl.getEntityManager().find(Value.class, 1L);
    Assert.assertNull(value);

  }

  @Test(expected = RollbackException.class)
  public void shouldNotStoreNullValue() throws Exception {
    Value v = new StringValue();
    pl.handleCreate(Value.class, v);
  }

  @Test
  public void shouldTestBooleanValue() throws Exception {
    BooleanValue v = new BooleanValue("true");
    Assert.assertTrue(v.getTypedValue());

    v = new BooleanValue("1");
    Assert.assertTrue(v.getTypedValue());

    v = new BooleanValue("yes");
    Assert.assertTrue(v.getTypedValue());

    v = new BooleanValue("false");
    Assert.assertFalse(v.getTypedValue());

    v = new BooleanValue("0");
    Assert.assertFalse(v.getTypedValue());

    v = new BooleanValue("no");
    Assert.assertFalse(v.getTypedValue());

    v = new BooleanValue("some random string");
    Assert.assertFalse(v.getTypedValue());
  }

  @Test
  public void shouldTestNamedQueries() throws Exception {
    DigitalCollection coll = new DigitalCollection("TestMe");
    pl.handleCreate(DigitalCollection.class, coll);

    Property p1 = new Property("mimetype");
    ValueSource vs = new ValueSource("source");

    Element e1 = new Element("e1", "path/to/file/e1");
    Element e2 = new Element("e2", "path/to/file/e2");
    Element e3 = new Element("e3", "path/to/file/e3");

    StringValue v1 = new StringValue("application/pdf");
    v1.setElement(e1);
    v1.setProperty(p1);
    v1.setSource(vs);
    v1.setMeasuredAt(new Date().getTime());

    StringValue v2 = new StringValue("document/txt");
    v2.setElement(e2);
    v2.setProperty(p1);
    v2.setSource(vs);
    v2.setMeasuredAt(new Date().getTime());

    StringValue v3 = new StringValue("application/pdf");
    v3.setElement(e2);
    v3.setProperty(p1);
    v3.setSource(vs);
    v3.setMeasuredAt(new Date().getTime());

    e1.getValues().add(v1);
    e1.setCollection(coll);
    e2.getValues().add(v2);
    e2.setCollection(coll);
    e3.getValues().add(v3);
    e3.setCollection(coll);

    coll.getElements().add(e1);
    coll.getElements().add(e2);
    coll.getElements().add(e3);

    pl.handleCreate(Element.class, e1);
    pl.handleCreate(Element.class, e2);
    pl.handleCreate(Element.class, e3);

    Query query = pl.getEntityManager().createNamedQuery(Constants.COLLECTION_VALUES_BY_PROPERTY_NAME_QUERY)
        .setParameter("pname", "mimetype").setParameter("coll", coll);
    List list = query.getResultList();
    Assert.assertEquals(3, list.size());

    query = pl.getEntityManager().createNamedQuery(Constants.COLLECTION_ELEMENTS_WITH_PROPERTY_AND_VALUE_COUNT_QUERY)
        .setParameter("pname", "mimetype").setParameter("value", "application/pdf").setParameter("coll", coll);
    Long count = (Long) query.getSingleResult();
    Assert.assertEquals(new Long(2), count);

    query = pl.getEntityManager().createNamedQuery(Constants.COLLECTION_DISTINCT_PROPERTY_VALUE_COUNT_QUERY)
        .setParameter("pname", "mimetype").setParameter("coll", coll);
    count = (Long) query.getSingleResult();
    Assert.assertEquals(new Long(2), count);

    query = pl.getEntityManager().createNamedQuery(Constants.COLLECTION_DISTINCT_PROPERTY_VALUES_SET_QUERY)
        .setParameter("pname", "mimetype").setParameter("coll", coll);
    list = query.getResultList();

    Assert.assertEquals("application/pdf", list.get(0));
    Assert.assertEquals("document/txt", list.get(1));

  }
}
