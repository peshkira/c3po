package com.petpet.c3po.dao;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.Source;
import com.petpet.c3po.utils.DataHelper;

public class DBCacheTest {

  private DBCache cache;
  private PersistenceLayer pl;
  private DBCursor cursor;

  @BeforeClass
  public static void beforeAllTests() {
    DataHelper.init();
  }

  @Before
  public void setup() {

    pl = mock(PersistenceLayer.class);
    cursor = mock(DBCursor.class);

    when(pl.find(Mockito.eq("properties"), Mockito.any(DBObject.class))).thenReturn(cursor);
    when(pl.find(Mockito.eq("sources"), Mockito.any(DBObject.class))).thenReturn(cursor);

    this.cache = new DBCache();
    this.cache.setPersistence(this.pl);

  }

  @Test
  public void shouldTestCacheMissAndDBMiss() throws Exception {
    when(cursor.count()).thenReturn(0);

    this.cache.clear();

    String key = "test";
    Property property = this.cache.getProperty(key);

    Assert.assertNotNull(property);
    Assert.assertNotNull(property.getId());
    Assert.assertEquals(key, property.getKey());
    verify(pl, Mockito.times(1)).find(Mockito.eq("properties"), Mockito.any(DBObject.class));

  }

  @Test
  public void shouldTestCacheMissAndDBHit() throws Exception {
    when(cursor.count()).thenReturn(1);
    when(cursor.next()).thenReturn(mock(DBObject.class));

    this.cache.clear();

    String key = "test";
    Property property = this.cache.getProperty(key);

    Assert.assertNotNull(property);
    verify(pl, Mockito.times(1)).find(Mockito.eq("properties"), Mockito.any(DBObject.class));
  }

  @Test
  public void shouldTestCacheHit() throws Exception {
    when(cursor.count()).thenReturn(0);

    this.cache.clear();

    Source s = this.cache.getSource("test", "v0.1");
    s = this.cache.getSource("test", "v0.1");

    Assert.assertNotNull(s);
    Assert.assertNotNull(s.getId());

    verify(pl, Mockito.times(1)).find(Mockito.eq("sources"), Mockito.any(DBObject.class));

  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowAnException() {
    when(cursor.count()).thenReturn(2);

    this.cache.clear();
    this.cache.getSource("test", "v0.1");

    Assert.fail("This code should not have been reached, failing test");
  }

}
