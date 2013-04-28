package com.petpet.c3po.api.adaptor;

public interface PreProcessingRule extends ProcessingRule {

  boolean shouldSkip(String property, String value, String status, String tool, String version);
  
}
