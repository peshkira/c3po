package com.petpet.c3po.dao.mongo;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Model;

public class MongoIterator<E extends Model> implements Iterator<E> {

  private Iterator<DBObject> iter;
  private ModelDeserializer deserializer;

  public MongoIterator(ModelDeserializer deserializer, DBCursor cursor) {
    if (cursor != null)
      this.iter = cursor.iterator();

    if (deserializer == null) {
      throw new IllegalArgumentException("Deserializer cannot be null");
    }
    
    this.deserializer = deserializer;
  }

  @Override
  public boolean hasNext() {
    if (this.iter == null) {
      return false;
    }

    return this.iter.hasNext();
  }

  @Override
  public E next() {
    if (this.iter == null) {
      throw new NoSuchElementException("The iterator was null");
    }

    DBObject next = this.iter.next();

    return (E) this.deserializer.deserialize(next);
  }

  @Override
  public void remove() {
    this.iter.remove();

  }

}
