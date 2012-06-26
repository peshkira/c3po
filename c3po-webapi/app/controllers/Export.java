package controllers;

import java.io.File;

import org.dom4j.Document;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.export;

import com.petpet.c3po.analysis.CSVGenerator;
import com.petpet.c3po.analysis.ProfileGenerator;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;

public class Export extends Controller {

  public static Result index() {
    return ok(export.render("c3po - Export Data", Application.getCollectionNames()));
  }

  public static Result profile() {
    Logger.debug("Received a profile generation call");
    final String accept = request().getHeader("Accept");

    Filter filter = Application.getFilterFromSession();

    if (filter == null) {
      final DynamicForm form = form().bindFromRequest();
      final String c = form.get("collection");

      if (c == null) {
        return badRequest("No collection parameter provided\n");
      } else if (!Application.getCollectionNames().contains(c)) {
        return notFound("No collection with name " + c + " was found\n");
      }

      filter = new Filter(c, null, null);
    }

    if (accept.contains("*/*") || accept.contains("application/xml")) {
      return profileAsXml(filter);
    }

    Logger.debug("The accept header is not supported: " + accept);
    return badRequest("The provided accept header '" + accept + "' is not supported");
  }

  public static Result csv() {
    PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    CSVGenerator generator = new CSVGenerator(p);
    return TODO;
  }

  private static Result profileAsXml(Filter filter) {
    Logger.debug("Generating profile for filter " + filter.getDocument());
    PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    ProfileGenerator generator = new ProfileGenerator(p);
    Document profile = generator.generateProfile(filter);
    generator.write(profile, "profiles/" + filter.getCollection() + ".xml");

    return ok(new File("profiles/" + filter.getCollection() + ".xml"));
  }
}
