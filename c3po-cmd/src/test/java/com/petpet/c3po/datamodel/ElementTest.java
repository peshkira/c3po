package com.petpet.c3po.datamodel;

import javax.persistence.Query;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.dao.LocalPersistenceLayer;

public class ElementTest {

  private static DigitalCollection coll;
  private static PersistenceLayer pl;

  @BeforeClass
  public static void beforeTests() {
//    EntityManagerFactory emf = Persistence.createEntityManagerFactory("LocalC3POPersistenceUnit");
    pl = new LocalPersistenceLayer();
    coll = new DigitalCollection("Test");
  }

  @Before
  public void before() {
  }

  @After
  public void after() {
  }

  @Test
  public void shouldStoreElement() throws Exception {
    String name = "TestElement";
    String path = "path/to/file";
    Element e = new Element();
    e.setName(name);
    e.setUid(path);
    e.setCollection(coll);
    coll.getElements().add(e);

    pl.handleCreate(DigitalCollection.class, coll);
    pl.handleCreate(Element.class, e);

    Element element = (Element) pl.handleFindById(Element.class, 1L);

    Assert.assertNotNull(element);
    Assert.assertEquals(name, element.getName());
    Assert.assertEquals(path, element.getUid());
  }

  @Test
  public void shouldUpdateElement() throws Exception {
    Element element = (Element) pl.handleFindById(Element.class, 1L);
    Assert.assertNotNull(element);

    String name = "New";
    element.setName(name);
    pl.handleCreate(Element.class, element);
    pl.getEntityManager().clear();

    element = (Element) pl.handleFindById(Element.class, 1L);
    Assert.assertNotNull(element);
    Assert.assertEquals(name, element.getName());
  }

  @Test
  public void shouldDeleteElement() throws Exception {

    Element element = pl.getEntityManager().find(Element.class, 1L);
    pl.handleDelete(Element.class, element);
    pl.getEntityManager().clear();

    element = (Element) pl.handleFindById(Element.class, 1L);
    Assert.assertNull(element);
  }

  @Test
  public void shouldCountElements() throws Exception {
    DigitalCollection coll = new DigitalCollection("TestMe");
    pl.handleCreate(DigitalCollection.class, coll);

    Element e1 = new Element();
    e1.setName("Element1");
    e1.setUid("path/to/file");
    e1.setCollection(coll);

    Element e2 = new Element();
    e2.setName("Element2");
    e2.setUid("path/to/other");
    e2.setCollection(coll);

    Element e3 = new Element();
    e3.setName("Element3");
    e3.setUid("path/file");
    e3.setCollection(coll);

    coll.getElements().add(e1);
    coll.getElements().add(e2);
    coll.getElements().add(e3);

    pl.handleCreate(Element.class, e1);
    pl.handleCreate(Element.class, e2);
    pl.handleCreate(Element.class, e3);

    Query query = pl.getEntityManager().createNamedQuery(Constants.COLLECTION_ELEMENTS_COUNT_QUERY)
        .setParameter("coll", coll);
    Long count = (Long) query.getSingleResult();

    Assert.assertEquals(new Long(3), count);
  }
}
