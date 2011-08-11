package com.petpet.collpro.utils;

import org.junit.Test;

import com.petpet.collpro.datamodel.BooleanValue;
import com.petpet.collpro.datamodel.NumericValue;
import com.petpet.collpro.datamodel.PropertyType;
import com.petpet.collpro.datamodel.StringValue;
import com.petpet.collpro.datamodel.Value;

import junit.framework.Assert;

public class HelperTest {
    
    @Test
    public void shouldGetCorrectValueType() throws Exception {
        Value value = Helper.getTypedValue(PropertyType.BOOL, "true");
        Assert.assertTrue(value instanceof BooleanValue);
        
        value = Helper.getTypedValue(PropertyType.BOOL, "this wont be true");
        Assert.assertTrue(value instanceof BooleanValue);
        Assert.assertFalse(((BooleanValue)value).getValue());
        
        value = Helper.getTypedValue(PropertyType.DEFAULT, "test");
        Assert.assertTrue(value instanceof StringValue);
        
        value = Helper.getTypedValue(PropertyType.STRING, "woot");
        Assert.assertTrue(value instanceof StringValue);
        
        value = Helper.getTypedValue(PropertyType.NUMERIC, "42");
        Assert.assertTrue(value instanceof NumericValue);
        
        value = Helper.getTypedValue(PropertyType.NUMERIC, "NAN");
        Assert.assertTrue(value instanceof NumericValue);
        
        value = Helper.getTypedValue(PropertyType.ARRAY, "fail");
        Assert.assertNull(value);
        
    }
}
