package com.petpet.c3po.tools.fits;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.petpet.c3po.adaptor.fits.FITSMetaDataAdaptor;
import com.petpet.c3po.api.Call;
import com.petpet.c3po.api.Message;
import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.db.DBManager;

public class FITSMetaDataAdaptorTest {

	private FITSMetaDataAdaptor converter;
	private DigitalCollection collection;

	@Before
	public void before() {
		this.converter = new FITSMetaDataAdaptor();
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

//		this.converter.addObserver(new Call() {
//
//			@Override
//			public void back(Message<?> n) {
//				if (n.getData() == null) {
//					Assert.fail("No element returned");
//				} else {
//					Assert.assertEquals(((Element) n.getData()).getName(), "About Stacks.pdf");
//				}
//
//			}
//		});
//
//		this.converter.configure(config);
//		this.converter.execute();
		
		Assert.fail("To be implemented");
	}

	@Test
	public void shouldTestParamters() throws Exception {
//		List<String> cParams = this.converter.getConfigParameters();
//		List<String> mParams = this.converter.getMandatoryParameters();

//		Assert.assertEquals(3, cParams.size());
//		Assert.assertEquals(2, mParams.size());
	  Assert.fail("to be implemented");
	}

	@Test
	public void shouldTestTimestampExtraction() throws Exception {
		Assert.fail("not implemented yet");
	}

}
