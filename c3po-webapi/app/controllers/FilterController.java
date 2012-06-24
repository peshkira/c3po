package controllers;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;

public class FilterController extends Controller{

  public static Result get(String uid) {
    Logger.info("Getting Filter Representation " + uid);
    PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    DBCursor cursor = p.find(Constants.TBL_FILTERS, new BasicDBObject("_id", uid));

    if (cursor.count() == 1) {
      return ok(cursor.next().toString());
    }
    
    return notFound("{error: 'Not Found'}");
  }
  
  public static Result remove(String uid) {
//    PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
//    DBCursor cursor = p.find(Constants.TBL_FILTERS, new BasicDBObject("_id", uid));
//    
//    if (cursor.count() == 1) {
//      Filter root = DataHelper.parseFilter(cursor.next());
//      
////      while (root.)
//    }
    return ok();
  }
}
