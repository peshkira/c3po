package com.petpet.c3po.utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.ActionLog;
import com.petpet.c3po.common.Constants;

public class ActionLogHelper {

  private PersistenceLayer persistence;

  public ActionLogHelper(PersistenceLayer p) {
    this.persistence = p;
  }
  
  public void recordAction(ActionLog action) {
    this.persistence.getDB().getCollection(Constants.TBL_ACTIONLOGS).remove(new BasicDBObject("collection", action.getCollection()));
    this.persistence.insert(Constants.TBL_ACTIONLOGS, action.getDocument());
  }
  
  public ActionLog getLastAction(String collection) {
    DBCursor cursor = this.persistence.find(Constants.TBL_ACTIONLOGS, new BasicDBObject("collection", collection));
    
    ActionLog result = null;
    
    if (cursor.count() == 1) {
      result = DataHelper.parseActionLog(cursor.next());
    } else if (cursor.count() > 1) {
      throw new RuntimeException("More than one action logs foudn for this collection");
    }
    
    return result;
  }
}
