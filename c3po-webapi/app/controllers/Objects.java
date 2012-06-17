package controllers;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.objects;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Configurator;

public class Objects extends Controller {

  private static final String ALL_COLLECTIONS = "All collections";
  private static final int QUERY_BATCH_SIZE = 25;

  public static Result index() {
    return list(0);
  }

  public static Result show(String id) {
    Logger.info("Select element with id: " + id);
    final List<String> names = Application.getCollectionNames();
    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    DBCursor cursor = pl.find(Constants.TBL_ELEMENTS, new BasicDBObject("_id", new ObjectId(id)));
    
    if (cursor.count() == 0) {
      Logger.info("Cursor selected " + cursor.count() );
      return notFound("No such element exists in the db.");
    } else if (cursor.count() > 1) {
      Logger.info("Cursor selected " + cursor.count() );
      return notFound("One or more objects with this id exist");
    } else {
      
      Element element = Element.parseElement(cursor.next());
      
      return ok(
          objects.render(element.getCollection(), names, element, null, 0, 0)
          );
    }
    
  }
  
  public static Result list(Integer offset) {
    Logger.info("List objects with offset: " + offset);
    return listObjectsInCollectionWithOffset(ALL_COLLECTIONS, "none", offset);
  }
  
  public static Result listObjectsInCollection(String name, String filter) {
    return listObjectsInCollectionWithOffset(name, filter, 0);
  }
  
  public static Result listObjectsInCollectionWithOffset(String name, String filter, Integer offset) {
    Logger.info("List objects in collection: " + name +" with offset: " + offset);
    
    final List<Element> result = new ArrayList<Element>();
    final List<String> names = Application.getCollectionNames();
    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    Property f = pl.getCache().getProperty(filter);
    
    BasicDBObject query = new BasicDBObject();
    if (!name.equals(ALL_COLLECTIONS)) {
      query.put("collection", name);
    }
    
    
    if (!filter.equals("none")) {
      query.put("metadata."+f.getId()+".value", "application/pdf"); //TODO change this...
    }
    
    final DBCursor cursor = pl.getDB().getCollection(Constants.TBL_ELEMENTS).find(query).skip(offset).limit(QUERY_BATCH_SIZE);
    
    Logger.info("Cursor has: " + cursor.count() + " objects");
    
    while (cursor.hasNext()) {
      final Element e = Element.parseElement(cursor.next());
      if (e.getName() == null) {
        e.setName("missing name");
      }
      
      result.add(e);
    }
    
    return ok(
        objects.render(name, names, null, result, QUERY_BATCH_SIZE, offset)
        );
    
    
  }
}
