package com.petpet.collpro.utils;

import java.io.File;

import org.junit.Test;

import junit.framework.Assert;

public class XMLUtilsTest {
    
    
    @Test
    public void shouldPassXMLValidation() throws Exception {
        boolean valid = XMLUtils.validate(new File("src/test/resources/valid.xml"));
        Assert.assertTrue(valid);
    }
    
    @Test
    public void shouldFailXMLValidation() throws Exception {
        boolean valid = XMLUtils.validate(new File("src/test/resources/invalid.xml"));
        Assert.assertFalse(valid);
    }
}
