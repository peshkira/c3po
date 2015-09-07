package template_configurator;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class configurationTest {
	Serializer serlzr = new Persister();
	configuration configWritten = new configuration();
	configuration configRead=null;
	File file = null;
	
	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
	
	@Before
	public void setUp() throws Exception {
		file=testFolder.newFile(".c3po.template_config");
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

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHashCode() {
		try {
			serlzr.write(configWritten, file);
			configRead = serlzr.read(configuration.class, file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(configWritten.hashCode() == configRead.hashCode());
	}
	
	@Test
	public void getPropsTest(){
		Map<String, String> mapFilter=new TreeMap<>();
		mapFilter.put("colorspace", "rgb");
		String[] props=configWritten.getProps(mapFilter);
		
		assertEquals(props.length, 6);
	}

	

}
