package com.petpet.c3po.dao.mongo;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Model;

/**
 * Wraps a Mongo {@link DBCursor} into an iterator.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 * @param <E>
 *          the model class that is wrapped.
 */
public class MongoIterator<E extends Model> implements Iterator<E> {

  /**
   * The internal iterator used.
   */
  private Iterator<DBObject> iter;

  /**
   * The deserializer for each object.
   */
  private MongoModelDeserializer deserializer;

  /**
   * Creates an iterator.
   * 
   * @param deserializer
   *          the deserializer to use.
   * @param cursor
   *          the cursor to use.
   * @throws IllegalArgumentException
   *           if the desearializer is null.
   */
  public MongoIterator(MongoModelDeserializer deserializer, DBCursor cursor) {
    if ( cursor != null )
      this.iter = cursor.iterator();

    if ( deserializer == null ) {
      throw new IllegalArgumentException( "Deserializer cannot be null" );
    }

    this.deserializer = deserializer;
  }

  /**
   * @return true if the iterator has a next element, false otherwise.
   */
  @Override
  public boolean hasNext() {
    if ( this.iter == null ) {
      return false;
    }

    return this.iter.hasNext();
  }

  /**
   * Returns the next element (after deserialization) if the iterator has a next
   * element. This method should be called only after
   * {@link MongoIterator#hasNext()} call has returned true.
   * 
   * @throws NoSuchElementException
   *           if there is no next element or the iterator is null.
   */
  @Override
  public E next() {
    if ( this.iter == null ) {
      throw new NoSuchElementException( "The iterator was null" );
    }

    DBObject next = this.iter.next();

    return (E) this.deserializer.deserialize( next );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove() {
    this.iter.remove();

  }

}
