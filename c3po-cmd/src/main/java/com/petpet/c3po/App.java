package com.petpet.c3po;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.GroupCommand;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.utils.Configurator;

public class App {

  /**
   * @param args
   */
  public static void main(String[] args) {
    final Map<String, String> dbconf = new HashMap<String, String>();
    dbconf.put("host", "localhost");
    dbconf.put("port", "27017");
    dbconf.put("db.name", "c3po");
   
    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure(dbconf);
    PersistenceLayer pLayer = configurator.getPersistence();
    
    final DBCollection elements = pLayer.getDB().getCollection("elements");

    final BasicDBObject keys = new BasicDBObject();
    keys.put("e7f05134-9008-44f3-b9aa-facd8c30f72f" + ".value", true);

//    final BasicDBObject condition = new BasicDBObject();
//    condition.put("collection", this.coll);

    final BasicDBObject initial = new BasicDBObject();
    initial.put("sum", 0);
    
    GroupCommand cmd = new GroupCommand(elements, keys, null, initial, Constants.HISTOGRAM_REDUCE, null);
    
    DBObject group = elements.group(cmd);
    System.out.println(group);
    
    if (!group.keySet().isEmpty()) {
      DBObject object = (DBObject) group.get("5");
      System.out.println(Arrays.deepToString(object.keySet().toArray()));
      System.out.println(object.get("sum"));
    }
   

  }

}
