package controllers;

import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import com.typesafe.plugin.*;

public class Mail extends Controller {

  public static Result send() {
    DynamicForm form = form().bindFromRequest();
    String email = form.get("email");
    String message = form.get("message");

    Logger.debug("sending feedback message from: " + email);

    String to = Play.application().configuration().getString("feedback.mail.to");
    String subject = Play.application().configuration().getString("feedback.mail.subject");

    MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
    mail.setSubject(subject).addRecipient(to).addFrom(email);
    try {
      mail.send(message);
    } catch (Exception e) {
      Logger.error(e.getMessage());
      e.printStackTrace();
      return internalServerError("Sending email failed: " + e.getMessage());
    }

    return ok();
  }
}
