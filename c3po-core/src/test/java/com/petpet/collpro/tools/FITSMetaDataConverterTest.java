package com.petpet.collpro.tools;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.petpet.collpro.api.Call;
import com.petpet.collpro.api.Message;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.db.DBManager;

public class FITSMetaDataConverterTest {

	private FITSMetaDataConverter converter;
	private DigitalCollection collection;

	@Before
	public void before() {
		this.converter = new FITSMetaDataConverter();
		this.collection = new DigitalCollection("Test");
		DBManager.getInstance().persist(this.collection);

	}

	@After
	public void after() {
		DBManager.getInstance().close();
		DBManager.getInstance().createEntityManagerFactory(); // reset db
	}

	@Test
	public void shouldExtractData() throws Exception {
		File[] files = { new File("src/test/resources/fits.xml") };
		Map<String, Object> config = new HashMap<String, Object>();
		config.put("config.date", new Date());
		config.put("config.collection", this.collection);
		config.put("config.fits_files", files);

		this.converter.addObserver(new Call() {

			@Override
			public void back(Message<?> n) {
				if (n.getData() == null) {
					Assert.fail("No element returned");
				} else {
					Assert.assertEquals(((Element) n.getData()).getName(), "About Stacks.pdf");
				}

			}
		});

		this.converter.configure(config);
		this.converter.execute();
	}

	@Test
	public void shouldTestParamters() throws Exception {
		List<String> cParams = this.converter.getConfigParameters();
		List<String> mParams = this.converter.getMandatoryParameters();

		Assert.assertEquals(3, cParams.size());
		Assert.assertEquals(2, mParams.size());
	}

	@Test
	public void shouldTestTimestampExtraction() throws Exception {
		Assert.fail("not implemented yet");
	}

}
