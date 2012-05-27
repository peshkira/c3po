package com.petpet.c3po.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.datamodel.Property;

public final class Helper {

  private static final Logger LOG = LoggerFactory.getLogger(Helper.class);

  public static Object getTypedValue(Property p, String value) {

    // check for booleans
    if (p.getKey().startsWith("has") || p.getKey().startsWith("is") || p.getKey().equals("well-formed")
        || p.getKey().equals("valid")) {
      return Helper.getBooleanValue(value);
    }

    //TODO check if date
    
    //TODO check if numeric
    
    //TODO check if array
    
    return value;

  }

  private static boolean getBooleanValue(String value) {
    if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) {
      return new Boolean(true);
    } else if (value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")) {
      return new Boolean(false);
    } else {
      LOG.warn("boolean value is probably not a real boolean, setting to false");
      return false;
    }
  }

  private Helper() {

  }
}
