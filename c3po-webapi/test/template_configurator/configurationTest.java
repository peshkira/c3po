package template_configurator;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.Configuration;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import junit.framework.TestCase;

public class configurationTest extends TestCase {

	public void setUp() throws Exception {
		super.setUp();
		List<template> templates = new ArrayList<template>();
		List<filter> filters = new ArrayList<filter>();

		template raster_image_template = new template();
		raster_image_template.setID("1");
		raster_image_template.setMessage("This is a template for raster images");
		raster_image_template.setName("raster_image_template");

		List<property> props = new ArrayList<property>();
		props.add(new property("format_version", "histogram"));
		props.add(new property("mimetype", "histogram"));
		props.add(new property("image_width", "histogram"));
		props.add(new property("image_height", "histogram"));
		props.add(new property("icc_profile_name", "histogram"));
		props.add(new property("exif_version", "histogram"));
		raster_image_template.setProperties(props);
		templates.add(raster_image_template);
		configWritten.setTemplates(templates);

		filter colorspaceRBG = new filter();
		colorspaceRBG.setTemplate_ID("1");

		List<condition> conditions = new ArrayList<condition>();
		conditions.add(new condition("colorspace", "rgb"));

		colorspaceRBG.setConditions(conditions);
		filters.add(colorspaceRBG);

		configWritten.setFilters(filters);
	}

	public void tearDown() throws Exception {
		
	}
	Serializer serlzr = new Persister();
	configuration configWritten = new configuration();
	configuration configRead=null;
	File result = new File("template_config.xml");
	public void testSetTemplates() {
		
		try {
			serlzr.write(configWritten, result);
			configRead = serlzr.read(configuration.class, result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(configWritten.hashCode() == configRead.hashCode());
		// fail("Not yet implemented");
	}

}
