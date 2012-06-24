package controllers;

import java.io.File;

import org.dom4j.Document;

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
    return ok(
        export.render("c3po - Export Data", Application.getCollectionNames())
        );
  }
  
  public static Result xml() {
    Filter filter = Application.getFilterFromSession();
    if (filter == null) {
      return notFound("No collection was selected");
    }
    
    PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    ProfileGenerator generator = new ProfileGenerator(p);
    Document profile = generator.generateProfile(filter);
    generator.write(profile, "profiles/" + filter.getCollection()+ ".xml");
    
    return ok(new File("profiles/" + filter.getCollection() + ".xml"));
  }
  
  public static Result csv() {
    PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
    CSVGenerator generator = new CSVGenerator(p);
    return TODO;
  }
}
