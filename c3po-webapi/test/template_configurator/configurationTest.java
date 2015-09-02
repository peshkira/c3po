package template_configurator;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class configurationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		
		Serializer serlzr=new Persister();
		configuration config=new configuration();
		List<template> templates=new ArrayList<template>();
		List<filter> filters=new ArrayList<filter>();
		
		template raster_image_template=new template();
		raster_image_template.setID("1");
		raster_image_template.setMessage("This is a template for raster images");
		raster_image_template.setName("raster_image_template");
		
		List<property> props=new ArrayList<property>();
		props.add(new property("format_version","histogram"));
		props.add(new property("mimetype","histogram"));
		props.add(new property("image_width","histogram"));
		props.add(new property("image_height","histogram"));
		props.add(new property("icc_profile_name","histogram"));
		props.add(new property("exif_version","histogram"));
		raster_image_template.setProperties(props);
		templates.add(raster_image_template);
		config.setTemplates(templates);
		
		filter colorspaceRBG=new filter();
		colorspaceRBG.setTemplate_ID("1");
		
		List<condition> conditions=new ArrayList<condition>();
		conditions.add(new condition("colorspace", "rgb"));
		
		colorspaceRBG.setConditions(conditions);
		filters.add(colorspaceRBG);
		
		config.setFilters(filters);
		
		File result=new File("config.xml");
		
		try {
			serlzr.write(config, result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fail("Not yet implemented");
	}

}
