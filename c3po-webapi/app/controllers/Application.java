package controllers;

import java.util.Collections;
import java.util.List;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;
import common.WebAppConstants;

public class Application extends Controller {

  public static final String[] PROPS = { "mimetype", "format", "format_version", "valid", "wellformed", "creating_application_name" };

  public static Result index() {
    return ok(index.render("c3po", getCollectionNames()));
  }

  public static Result setCollection(String c) {
    Logger.debug("Received collection setup change for " + c);
    final PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    final List<String> names = getCollectionNames();

    if (c == null || c.equals("") || !names.contains(c)) {
      return notFound("No collection '" + c + "' was found");
    }

    BasicDBObject query = new BasicDBObject("collection", c);
    query.put("property", null);
    query.put("value", null);

    DBCursor cursor = p.find(Constants.TBL_FILTERS, query);
    Filter f;
    if (cursor.count() == 0) {
      f = new Filter(c, null, null);
      p.insert(Constants.TBL_FILTERS, f.getDocument());
    } else {
      f = DataHelper.parseFilter(cursor.next());
    }

    session().clear();
    session().put(WebAppConstants.CURRENT_COLLECTION_SESSION, c);
    session().put(WebAppConstants.CURRENT_FILTER_SESSION, f.getId());
    return ok("The collection was changed successfully\n");
  }

  public static Result removeLastFilter() {
    final PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    final Filter filter = getFilterFromSession();
    Logger.debug("Removing last filter");

    if (filter != null) {
      Filter parent = filter.getParent();
      if (parent == null) {
        Logger.debug("Removing all");
        session().clear();
      } else {
        Logger.debug("Removing this");
        session().put(WebAppConstants.CURRENT_FILTER_SESSION, parent.getId());
        p.getDB().getCollection(Constants.TBL_FILTERS).remove(filter.getDocument());
      }
    }

    return ok();
  }

  public static Result getCollections() {
    Logger.debug("Received a get collections call");
    final String accept = request().getHeader("Accept");

    if (accept.contains("*/*") || accept.contains("application/xml")) {
      return collectionsAsXml();
    } else if (accept.contains("application/json")) {
      return collectionsAsXml();
    }

    return badRequest("The accept header is not supported");
  }
  
  public static Result getProperties() {
    Logger.debug("Received a get properties call");
    final String accept = request().getHeader("Accept");
    
    if (accept.contains("*/*") || accept.contains("application/xml")) {
      return TODO;
    } else if (accept.contains("application/json")) {
      return propertiesAsJson();
    }
    
    return badRequest("The accept header is not supported");
  }

  private static Result propertiesAsJson() {
    PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    List<String> properties = p.distinct(Constants.TBL_PROEPRTIES, "key");
    return ok(play.libs.Json.toJson(properties));
  }

  public static Result collectionsAsXml() {
    final List<String> names = Application.getCollectionNames();
    final StringBuffer resp = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

    resp.append("<collections>\n");

    for (String s : names) {
      resp.append("<collection name=\"" + s + "\" />\n");
    }

    resp.append("</collections>\n");

    response().setContentType("text/xml");

    return ok(resp.toString());
  }

  public static Result collectionsAsJson() {
    final List<String> names = Application.getCollectionNames();
    response().setContentType("application/json");
    return ok(play.libs.Json.toJson(names));
  }

  public static List<String> getCollectionNames() {
    PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
    List<String> collections = (List<String>) persistence.distinct(Constants.TBL_ELEMENTS, "collection");
    Collections.sort(collections);
    return collections;

  }

  public static Filter getFilterFromSession() {
    PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    String f = session().get(WebAppConstants.CURRENT_FILTER_SESSION);
    if (f != null) {
      DBCursor cursor = pl.find(Constants.TBL_FILTERS, new BasicDBObject("_id", f));
      if (cursor.count() == 1) {
        Filter filter = DataHelper.parseFilter(cursor.next());
        return filter;
      } else if (cursor.count() > 1) {
        Logger.error("More than one filter found");
        throw new RuntimeException("Found more than one filters with the same id");
      }
    }

    return null;
  }

  public static BasicDBObject getFilterQuery(Filter filter) {
    BasicDBObject query = new BasicDBObject("collection", filter.getCollection());
    Filter tmp = filter;
    do {

      if (tmp.getProperty() == null || tmp.getValue() == null) {
        tmp = tmp.getParent();
        continue;
      } else {
        if (tmp.getValue().equals("Unknown")) {
          query.put("metadata." + tmp.getProperty() + ".value", new BasicDBObject("$exists", false));
        } else {
          query.put("metadata." + tmp.getProperty() + ".value", inferValue(tmp.getValue()));
        }
      }

      tmp = tmp.getParent();
    } while (tmp != null);

    System.out.println("FilterQuery: " + query);
    return query;
  }

  public static Result clear() {
    session().clear();

    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    // final List<String> names = controllers.Application.getCollectionNames();
    // for (String name : names) {
    // DBCollection c = pl.getDB().getCollection("statistics_" + name);
    // c.drop();
    // for (String p : PROPS) {
    // c = pl.getDB().getCollection("histogram_" + name + "_" + p);
    // c.drop();
    // }
    // }

    pl.getDB().getCollection(Constants.TBL_FILTERS).drop();

    return redirect("/c3po");
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