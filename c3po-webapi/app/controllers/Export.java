package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.export;

public class Export extends Controller {

  public static Result index() {
    return ok(
        export.render("c3po - Export Data", Application.getCollectionNames())
        );
  }
}
