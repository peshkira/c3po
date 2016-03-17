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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
//import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.data.Form;
import play.mvc.Result;
import views.html.export;


import com.mongodb.BasicDBObject;
import com.petpet.c3po.analysis.CSVGenerator;
import com.petpet.c3po.analysis.ProfileGenerator;
import com.petpet.c3po.analysis.RepresentativeAlgorithmFactory;
import com.petpet.c3po.analysis.RepresentativeGenerator;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
//import com.petpet.c3po.datamodel.ActionLog;
//import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.ActionLogHelper;
import com.petpet.c3po.utils.Configurator;

import common.WebAppConstants;

import com.petpet.c3po.api.model.ActionLog;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;


public class Export extends Controller {

	public static Result index() {
		Logger.debug("Received an index call in export");
		return ok(export.render("c3po - Export Data", PropertyController.getCollectionNames(),Templates.getTemplates()));//export.render("c3po - Export Data", Application.getCollectionNames()));
	}

	public static Result profile() {
		Logger.debug("Received a profile generation call");
		final String accept = request().getHeader("Accept");

		final DynamicForm form = play.data.Form.form().bindFromRequest();
		final String c = form.get("collection");
		final String e = form.get("includeelements");

		Filter filter = FilterController.getFilterFromSession();
		boolean include = false;

		if (filter == null) {
			if (c == null) {
				return badRequest("No collection parameter provided\n");
			} else if (!PropertyController.getCollectionNames().contains(c)) {
				return notFound("No collection with name " + c + " was found\n");
			}

			filter = new Filter();
			filter.addFilterCondition(new FilterCondition("collection", c));
		}

		if (e != null) {
			include = Boolean.valueOf(e);
		}

		if (accept.contains("*/*") || accept.contains("application/xml")) {
			return profileAsXml(filter, include);
		}

		Logger.debug("The accept header is not supported: " + accept);
		return badRequest("The provided accept header '" + accept + "' is not supported");
	}

	public static Result exportAllToCSV() {
		Logger.debug("Received an exportAllToCSV call");
		CSVGenerator generator = getGenerator();
		String collection=PropertyController.getCollection();
		String path = "exports/" + collection + "_" + session(WebAppConstants.SESSION_ID) + "_matrix.csv";
		generator.exportAll(collection, path);

		File file = new File(path);

		try {
			return ok(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			return internalServerError(e.getMessage());
		}
	}

	public static Result exportFilterToCSV() {
		Logger.debug("Received an exportFilterToCSV call");
		CSVGenerator generator = getGenerator();
		Filter filter = FilterController.getFilterFromSession();
		String collection=PropertyController.getCollection();
		String path = "exports/" + collection + "_" + session(WebAppConstants.SESSION_ID) + "_matrix.csv";
		generator.export(filter, path);

		File file = new File(path);

		try {
			return ok(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			return internalServerError(e.getMessage());
		}
	}

	private static CSVGenerator getGenerator() {
		PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
		CSVGenerator generator = new CSVGenerator(p);

		return generator;

	}

	private static Result profileAsXml(Filter filter, boolean includeelements) {
		Logger.debug("Received a profileAsXml call in export");
		File result = generateProfile(filter, includeelements);

		return ok(result);
	}

	private static File generateProfile(Filter filter, boolean includeelements) {
		StringBuilder pathBuilder = new StringBuilder();
		String collection=PropertyController.getCollection();
		String path = "profiles/" + collection + "_" + session(WebAppConstants.SESSION_ID) + "_profile.xml";
		Logger.debug("Looking for collection profile " + path);

		File file = new File(path);

		if (!file.exists() || isCollectionUpdated(collection) || isNewFilter(file, filter)) {
			Logger.debug("File does not exist. Generating profile for filter " + filter.toString());
			Configurator configurator = Configurator.getDefaultConfigurator();
			PersistenceLayer p = configurator.getPersistence();
			String alg = configurator.getStringProperty("c3po.samples.algorithm");
			RepresentativeGenerator samplesGen = new RepresentativeAlgorithmFactory().getAlgorithm(alg);
			ProfileGenerator generator = new ProfileGenerator(p, samplesGen);
			Document profile = generator.generateProfile(filter);

			generator.write(profile, path);
			file = new File(path);

			ActionLogHelper alHelper = new ActionLogHelper(p);
			alHelper.recordAction(new ActionLog(collection, ActionLog.ANALYSIS_ACTION));
		}

		return file;
	}

	private static boolean isCollectionUpdated(String collection) {
		PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();
		ActionLogHelper alHelper = new ActionLogHelper(p);
		ActionLog lastAction = alHelper.getLastAction(collection);

		boolean isUpdated = true;

		if (lastAction != null) {
			if (lastAction.getAction().equals(ActionLog.UPDATED_ACTION)) {
				isUpdated = false;
			}
		}

		return isUpdated;
	}

	private static boolean isNewFilter(File file, Filter filter) {
		long profileFiltersCount = -1;
		long profileObjectsCount = -1;

		boolean isNew = true;
		try {
			final SAXReader reader = new SAXReader();
			Document doc = reader.read(file);
			org.dom4j.Element partition = doc.getRootElement().element("partition");
			profileFiltersCount = partition.element("filter").element("parameters").elements().size();
			profileObjectsCount = Long.parseLong(partition.attributeValue("count"));

		} catch (final DocumentException e) {
			//do nothing...
			//just regenerate the profile...
		}



		return isNew; 

	}

}
