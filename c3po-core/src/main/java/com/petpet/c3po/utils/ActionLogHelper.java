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
package com.petpet.c3po.utils;

import java.util.Iterator;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.ActionLog;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;

/**
 * A helper class to record and read the last action done over a collection of
 * elements.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class ActionLogHelper {

  /**
   * The persistence layer to use.
   */
  private PersistenceLayer persistence;

  /**
   * Creates an action log helper.
   * 
   * @param p
   */
  public ActionLogHelper(PersistenceLayer p) {
    this.persistence = p;
  }

  /**
   * Records the action. If it is updated, the persistence cache is cleared.
   * 
   * @param action
   */
  public void recordAction( ActionLog action ) {
    ActionLog lastAction = this.getLastAction( action.getCollection() );
    this.persistence.remove( lastAction );
    this.persistence.insert( action );

    if ( this.isLastActionUpdated( action.getCollection() ) ) {
      this.persistence.clearCache();
    }
  }

  /**
   * Checks if the last action for the given collection was
   * {@link ActionLog#UPDATED_ACTION}.
   * 
   * @param collection
   *          the collection to look for.
   * @return true if it was updated or null, false otherwise.
   */
  public boolean isLastActionUpdated( String collection ) {
    ActionLog lastAction = this.getLastAction( collection );
    boolean result = false;
    if ( lastAction == null || lastAction.getAction().equals( ActionLog.UPDATED_ACTION ) ) {
      result = true;
    }

    return result;
  }

  /**
   * Obtains the last action for the given collection.
   * 
   * @param collection
   *          the collection to look for
   * @return the last action or null if nothing was recorded yet.
   * @throws RuntimeException
   *           if more than one action logs wer found for the given collection.
   */
  public ActionLog getLastAction( String collection ) {
    Iterator<ActionLog> i = this.persistence.find( ActionLog.class, new Filter( new FilterCondition( "collection",
        collection ) ) );

    ActionLog result = null;

    if ( i.hasNext() ) {
      result = i.next();

      if ( i.hasNext() ) {
        this.persistence.remove( ActionLog.class, new Filter( new FilterCondition( "collection", collection ) ) );
        throw new RuntimeException( "More than one action logs found for this collection" );
      }
    }

    return result;
  }
}
