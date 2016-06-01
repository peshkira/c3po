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

import java.util.UUID;

import com.petpet.c3po.api.model.helper.Filter;

import common.WebAppConstants;
import helpers.SessionFilters;
import helpers.TemplatesLoader;
import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {

	public static String[] PROPS = { "mimetype", "format", "format_version", "valid", "wellformed",
			"creating_application_name", "created" };
	private static boolean initialized;
	public static void buildSession() {
		if (initialized)
			return;
		initialized=true;
		String session = session(WebAppConstants.SESSION_ID);
		Logger.debug("Building a new session with id:'" + session + "'");
		if (session == null) {
			session(WebAppConstants.SESSION_ID, UUID.randomUUID().toString());
			session(WebAppConstants.CURRENT_FILTER_SESSION, WebAppConstants.SESSION_ID);
		}
		Filter f=new Filter();
		SessionFilters.addFilter(session, f);
		Application.PROPS = TemplatesLoader.getDefaultTemplate();
	}

	public static Result clear() {
		Logger.debug("Received a clear call");
		session().clear();
		return redirect("/c3po");
	}
	public static Result getSetting(String key) {
		Logger.debug("Received a getSetting call");
		String value = session(key);
		if (value==null)
			value ="all";
		return ok(play.libs.Json.toJson(value));
	}

	public static Result index() {
		Logger.debug("Received an index call in application");
		buildSession();
		return ok(index.render("c3po", Properties.getCollectionNames()));
	}

	private static Object inferValue(String value) {
		Logger.debug("Inferring value of '" +value+ "'");
		Object result = value;
		if (value.equalsIgnoreCase("true")) {
			result = new Boolean(true);
		}

		if (value.equalsIgnoreCase("false")) {
			result = new Boolean(false);
		}

		return result;
	}
	public static Result setSetting() {

		DynamicForm form = play.data.Form.form().bindFromRequest();
		String setting = form.get("setting");
		String value = form.get("value");
		Logger.debug("Received a setSetting call for '" + setting + "' to value: '" + value + "'");
		session().put(setting, value);

		return ok();
	}
}
