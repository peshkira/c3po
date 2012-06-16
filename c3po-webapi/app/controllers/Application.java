package controllers;

import java.util.ArrayList;
import java.util.List;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.utils.Configurator;

import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {
  
  public static class Filter {
    public String name;
    public String filter;
  }
  
  public static Result index() {
    PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
    List collections = persistence.distinct(Constants.TBL_ELEMENTS, "collection");
    
    Logger.info("Size is " + collections.size());
    for (Object o : collections) {
      Logger.info(o.toString());
    }
    
    return ok(index.render("none", collections, form(Filter.class)));
  }
  
  public static Result select() {
    Form<Filter> form = form(Filter.class).bindFromRequest();
    if(form.hasErrors()) {
        return TODO; //badRequest(index.render(form));
    } else {
        Filter data = form.get();
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        List collections = persistence.distinct(Constants.TBL_ELEMENTS, "collection");
        return ok(
            index.render(data.name, collections, form(Filter.class))
        );
    }
  }
  
  
}