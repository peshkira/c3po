/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package controllers;

import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.data.Form;
import play.mvc.Result;
import com.typesafe.plugin.*;

public class Mail extends Controller {

  public static Result send() {
    DynamicForm form = play.data.Form.form().bindFromRequest();
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
