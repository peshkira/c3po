package com.petpet.c3po.adaptor.rules.drools;

/**
 * This is just a little helper to create CSV compatible output.  
 */
public class CSVParser {

  public static String prepareLine(Character csvSeperator, Character csvLimiter, String... values) {
    StringBuilder builder = new StringBuilder();
    
    for (String string : values) {
      builder.append(csvLimiter);
      builder.append(string);
      builder.append(csvLimiter);
      builder.append(csvSeperator);
    }
    return builder.toString();
  }

}
