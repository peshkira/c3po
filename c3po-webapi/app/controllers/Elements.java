package controllers;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.element;
import views.html.elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;
import common.WebAppConstants;

public class Elements extends Controller {

  public static Result index() {
    return list();

  }

  public static Result list() {
    String collection = session().get(WebAppConstants.CURRENT_COLLECTION_SESSION);

    int batch = getQueryParameter("batch", 25);
    int offset = getQueryParameter("offset", 0);

    return listElements(collection, batch, offset);

  }

  private static int getQueryParameter(String parameter, int dflt) {
    String[] strings = request().queryString().get(parameter);
    if (strings == null || strings.length == 0) {
      return dflt;
    }

    return Integer.parseInt(strings[0]);

  }

  public static Result show(String id) {
    Logger.info("Select element with id: " + id);
    final List<String> names = Application.getCollectionNames();
    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    DBCursor cursor = pl.find(Constants.TBL_ELEMENTS, new BasicDBObject("_id", new ObjectId(id)));

    if (cursor.count() == 0) {
      Logger.info("Cursor selected " + cursor.count());
      return notFound("No such element exists in the db.");
    } else if (cursor.count() > 1) {
      Logger.info("Cursor selected " + cursor.count());
      return notFound("One or more objects with this id exist");
    } else {

      Element elmnt = DataHelper.parseElement(cursor.next(), pl);

      return ok(element.render(names, elmnt));
    }

  }

  public static Result listElements(String collection, int batch, int offset) {
    final List<String> names = Application.getCollectionNames();

    if (collection == null) {
      return ok(elements.render(names, null));
    }

    final List<Element> result = new ArrayList<Element>();
    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();

    BasicDBObject query = new BasicDBObject();
    query.put("collection", collection);
    Filter filter = Application.getFilterFromSession();
    if (filter != null) {
      query = Application.getFilterQuery(filter);
      System.out.println("Objects Query: " + query);
    }

    final DBCursor cursor = pl.getDB().getCollection(Constants.TBL_ELEMENTS).find(query).skip(offset).limit(batch);

    Logger.info("Cursor has: " + cursor.count() + " objects");

    while (cursor.hasNext()) {
      final Element e = DataHelper.parseElement(cursor.next(), pl);

      if (e.getName() == null) {
        e.setName("missing name");
      }

      result.add(e);
    }

    return ok(elements.render(names, result));
  }

}
