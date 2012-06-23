package controllers;

import java.util.Collections;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;

public class Application extends Controller {
  
  public static final String[] PROPS = { "mimetype", "format", "valid", "wellformed" };

  public static Result index() {
    return ok(
        index.render("c3po", getCollectionNames())
        );
  }
  
  public static Result setCollection(String c) {
    System.out.println("Received collection setup change for " + c);
    session().clear();
    session().put("current.collection", c);
    return ok("The collection was changed successfully");
  }
  
  public static List<String> getCollectionNames() {
    PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
    List<String> collections = (List<String>) persistence.distinct(Constants.TBL_ELEMENTS, "collection");
    Collections.sort(collections);
    return collections;

  }
  
  public static BasicDBObject getFilterQuery(Filter filter) {
    BasicDBObject query = new BasicDBObject("collection", filter.getCollection());
    Filter tmp = filter;
    do {
      if (tmp.getValue().equals("Unknown")) {
        query.put("metadata." + tmp.getProperty() + ".value", new BasicDBObject("$exists", false));
      } else {
        query.put("metadata." + tmp.getProperty() + ".value", inferValue(tmp.getValue()));
      }
      tmp = tmp.getParent();
    } while (tmp != null);

    System.out.println("FilterQuery: " + query);
    return query;
  }
  
  public static Result clear() {
    session().clear();

    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    final List<String> names = controllers.Application.getCollectionNames();
    for (String name : names) {
      DBCollection c = pl.getDB().getCollection("statistics_" + name);
      c.drop();
      for (String p : PROPS) {
        c = pl.getDB().getCollection("histogram_" + name + "_" + p);
        c.drop();
      }
    }
    
    pl.getDB().getCollection(Constants.TBL_FILTERS).drop();
    
    return index();
  }
  
  private static Object inferValue(String value) {
    Object result = value;
    if (value.equalsIgnoreCase("true")) {
      result = new Boolean(true);
    }
    
    if (value.equalsIgnoreCase("false")) {
      result = new Boolean(false);
    }
    
    return result;
  }
}