package com.petpet.c3po.utils;

import java.util.Iterator;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.ActionLog;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;

public class ActionLogHelper {

  private PersistenceLayer persistence;

  public ActionLogHelper(PersistenceLayer p) {
    this.persistence = p;
  }

  public void recordAction(ActionLog action) {
    ActionLog lastAction = this.getLastAction(action.getCollection());
    this.persistence.remove(lastAction);
    this.persistence.insert(action);
  }

  public ActionLog getLastAction(String collection) {
    Iterator<ActionLog> i = this.persistence.find(ActionLog.class, new Filter(new FilterCondition("collection",
        collection)));

    ActionLog result = null;

    if (i.hasNext()) {
      result = i.next();

      if (i.hasNext())
        this.persistence.remove(ActionLog.class, new Filter(new FilterCondition("collection", collection)));
      throw new RuntimeException("More than one action logs foudn for this collection");
    }

    return result;
  }
}
