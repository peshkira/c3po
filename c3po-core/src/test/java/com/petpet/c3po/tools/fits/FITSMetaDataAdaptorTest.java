package com.petpet.c3po.tools.fits;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.petpet.c3po.adaptor.fits.FITSMetaDataAdaptor;
import com.petpet.c3po.datamodel.DigitalCollection;

public class FITSMetaDataAdaptorTest {

	private FITSMetaDataAdaptor converter;
	private DigitalCollection collection;

	@Before
	public void before() {
		this.converter = new FITSMetaDataAdaptor();
		this.collection = new DigitalCollection("Test");
	}

	@Test
	public void shouldExtractData() throws Exception {
		File[] files = { new File("src/test/resources/fits.xml") };
	
		Assert.fail("To be implemented");
	}

	@Test
	public void shouldTestTimestampExtraction() throws Exception {
		Assert.fail("not implemented yet");
	}

}
