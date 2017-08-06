package helpers.template_configurator;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import helpers.TemplatesLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class configurationTest {
	Serializer serlzr = new Persister();
	TemplatesLoader configWritten = new TemplatesLoader();
	TemplatesLoader configRead=null;
	File file = null;
	
	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
	
	@Before
	public void setUp() throws Exception {
		file=testFolder.newFile(".c3po.template_config");
		List<Template> templates = new ArrayList<Template>();
		List<TemplateFilter> filters = new ArrayList<TemplateFilter>();

		Template raster_image_template = new Template();
		raster_image_template.setID("1");
		raster_image_template.setMessage("This is a template for raster images");
		raster_image_template.setName("raster_image_template");

		List<TemplateProperty> props = new ArrayList<TemplateProperty>();
		props.add(new TemplateProperty("format_version", "histogram"));
		props.add(new TemplateProperty("mimetype", "histogram"));
		props.add(new TemplateProperty("image_width", "histogram"));
		props.add(new TemplateProperty("image_height", "histogram"));
		props.add(new TemplateProperty("icc_profile_name", "histogram"));
		props.add(new TemplateProperty("exif_version", "histogram"));
		raster_image_template.setProperties(props);
		templates.add(raster_image_template);
		configWritten.setTemplates(templates);

		TemplateFilter colorspaceRBG = new TemplateFilter();
		colorspaceRBG.setTemplate_ID("1");

		List<TemplateCondition> conditions = new ArrayList<TemplateCondition>();
		conditions.add(new TemplateCondition("colorspace", "rgb"));

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
			configRead = serlzr.read(TemplatesLoader.class, file);
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
		String[] props= configWritten.getProps(mapFilter).toArray(new String[0]);
		
		assertEquals(props.length, 6);
	}

	

}
