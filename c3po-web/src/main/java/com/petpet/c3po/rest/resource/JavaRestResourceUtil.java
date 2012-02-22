package com.petpet.c3po.rest.resource;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JavaRestResourceUtil {
	  public int getInt(int minVal, int maxVal, int defaultValue, String inputString) {
	    int output = defaultValue;
	    try {
	    	output = Integer.parseInt(inputString);
	    }
	    catch (Exception e){
	      output = defaultValue;
	    }

	    if (output < minVal) output = minVal;
	    if (maxVal == -1) {
	    	if (output < minVal) output = minVal;
	    }
	    else if (output > maxVal) output = maxVal;
	    return output;
	  }

	  public long getLong(long minVal, long maxVal, long defaultValue, String inputString) {
	    long output = defaultValue;
	    try {
	    	output = Long.parseLong(inputString);
	    }
	    catch (Exception e){
	      output = defaultValue;
	    }

	    if (output < minVal) output = minVal;
	    if (maxVal == -1) { if (output < minVal) output = minVal; }
	    else if (output > maxVal) output = maxVal;
	    return output;
	  }

	  public double getDouble(double minVal, double maxVal, double defaultValue, String inputString) {
	    double output = defaultValue;
	    try {
	    	output = Double.parseDouble(inputString);
	    }
	    catch (Exception e){
	      output = defaultValue;
	    }

	    if (output < minVal) output = minVal;
	    if (maxVal == -1) { 
	    	if (output < minVal) output = minVal; 
	    }
	    else if (output > maxVal) output = maxVal;
	    return output;
	  }

	  public boolean getBoolean(boolean defaultValue, String booleanString) {
	    boolean output = defaultValue;
	    if (booleanString == null) output = defaultValue;

	    //	treat "", "YES" as "true"
	    if ("".equals(booleanString)) output = true;
	    else if ("YES".equalsIgnoreCase(booleanString)) output = true;
	    else if ("NO".equalsIgnoreCase(booleanString)) output = false;
	    else {
	      try {
	    	  output = Boolean.parseBoolean(booleanString);
	      }
	      catch (Exception e){
	        output = defaultValue;
	      }
	    }
	    return output;
	  }
	  
	  public Date getDate(Date defaultValue, String dateString){
	    try {
	    	return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
	    }
	    catch(Exception e) {
	      return defaultValue;
	    }
	  }
	}
