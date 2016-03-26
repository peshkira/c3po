package controllers;

import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.conflicts;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by artur on 23/03/16.
 */
public class Conflicts extends Controller {
    public static Result index() {
        return ok(conflicts.render("c3po - conflict resolution", new ArrayList<String>()));
    }

    public static Result createRule() {
        DynamicForm form = play.data.Form.form().bindFromRequest();
        String input = form.get("input");
        String text = form.get("text");
        return ok();
    }

    public static Result deleteRule() {
        return play.mvc.Results.TODO;
    }

    public static Result getRules() {
        return ok();
    }

    public static Result update(){
        return ok();
    }
}
