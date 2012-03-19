package com.petpet.c3po.utils;

import junit.framework.Assert;

import org.junit.Test;

import com.petpet.c3po.datamodel.BooleanValue;
import com.petpet.c3po.datamodel.IntegerValue;
import com.petpet.c3po.datamodel.PropertyType;
import com.petpet.c3po.datamodel.StringValue;
import com.petpet.c3po.datamodel.Value;

public class HelperTest {
    
    @Test
    public void shouldGetCorrectValueType() throws Exception {
        Value<?> value = Helper.getTypedValue(PropertyType.BOOL.getClazz(), "true");
        Assert.assertTrue(value instanceof BooleanValue);
        
        value = Helper.getTypedValue(PropertyType.BOOL.getClazz(), "this wont be true");
        Assert.assertTrue(value instanceof BooleanValue);
        Assert.assertFalse(((BooleanValue)value).getTypedValue());
        
        value = Helper.getTypedValue(PropertyType.DEFAULT.getClazz(), "test");
        Assert.assertTrue(value instanceof StringValue);
        
        value = Helper.getTypedValue(PropertyType.STRING.getClazz(), "woot");
        Assert.assertTrue(value instanceof StringValue);
        
        value = Helper.getTypedValue(PropertyType.NUMERIC.getClazz(), "42");
        Assert.assertTrue(value instanceof IntegerValue);
        
        value = Helper.getTypedValue(PropertyType.NUMERIC.getClazz(), "NAN");
        Assert.assertTrue(value instanceof IntegerValue);
        
        value = Helper.getTypedValue(PropertyType.ARRAY.getClazz(), "a b c");
        Assert.assertTrue(value instanceof StringValue); //for now casted to stringvalue
        
    }
}
