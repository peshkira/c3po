package controllers;

import java.util.Collections;
import java.util.List;

import com.avaje.ebean.validation.AssertFalse;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.utils.Configurator;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {

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
  
  public static Result clearSession() {
    session().clear();
    return index();
  }
}