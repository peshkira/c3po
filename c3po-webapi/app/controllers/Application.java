package controllers;

import java.util.Collections;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import com.mongodb.DBCollection;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
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
    session().put("current.collection", c);
    return ok("The collection was changed successfully");
  }
  
  public static List<String> getCollectionNames() {
    PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
    List<String> collections = (List<String>) persistence.distinct(Constants.TBL_ELEMENTS, "collection");
    Collections.sort(collections);
    return collections;

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
    
    return index();
  }
}