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
