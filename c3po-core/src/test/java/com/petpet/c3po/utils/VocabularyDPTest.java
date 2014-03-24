package com.petpet.c3po.utils;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by artur on 3/24/14.
 */
public class VocabularyDPTest extends TestCase {
    public void testGetPropertyKeyByFitsName() throws Exception {
        VocabularyDP.init();
        String s=VocabularyDP.getUriByName("image_height");
        Assert.assertNotNull(s);
    }
}
