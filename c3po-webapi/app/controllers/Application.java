package controllers;

import helpers.Filter;
import helpers.Statistics;

import java.util.Collections;
import java.util.List;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.utils.Configurator;

public class Application extends Controller {

  public static Result index() {
    List<String> collections = getCollectionNames();

    return ok(index.render(null, collections, form(Filter.class), null));
  }

  public static Result show(String name) {
    Logger.info("Called Show with name" + name);
    
    List<String> collections = getCollectionNames();
    Statistics stats = getCollectionStatistics(name);
    return ok(index.render(name, collections, form(Filter.class), stats));
  }

  public static Result show(String name, String filter) {
    Logger.info("Called Show with name " + name + " and filter "+ filter);
    List<String> collections = getCollectionNames();
    Statistics stats = getCollectionStatistics(name);
    return ok(index.render(name, collections, form(Filter.class), stats));
  }

  private static List<String> getCollectionNames() {
    PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
    List<String> collections = (List<String>) persistence.distinct(Constants.TBL_ELEMENTS, "collection");
    Collections.sort(collections);

    // collections.add(0, "Select Collection");

    return collections;

  }

  private static Statistics getCollectionStatistics(String name) {
    Statistics stats = new Statistics();
    stats.setCount(12323);
    stats.setSize(27582172636239L);
    stats.setAvg(273.12D);
    stats.setMin(42);
    stats.setMax(12327105731L);
    stats.setSd(5652D);
    stats.setVar(213234D);
    return stats;
  }

}