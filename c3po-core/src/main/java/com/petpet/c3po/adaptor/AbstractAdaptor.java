package com.petpet.c3po.adaptor;

import java.util.Map;

import com.petpet.c3po.controller.Controller;

public abstract class AbstractAdaptor implements Runnable {

  public static final String UNKNOWN_COLLECTION_ID = "unknown";

  private Controller controller;

  private Map<String, Object> config;

  protected Controller getController() {
    return this.controller;
  }

  public void setController(Controller controller) {
    this.controller = controller;
  }

  protected Map<String, Object> getConfig() {
    return this.config;
  }
  
  protected void setConfig(Map<String, Object> config) {
    this.config = config;
  }

  protected String getStringConfig(String key, String defaultValue) {
    String result = null;
    if (this.config != null) {
      result = (String) this.config.get(key);
    }

    if (result == null) {
      result = defaultValue;
    }

    return result;
  }

  protected Boolean getBooleanConfig(String key, boolean defaultValue) {
    Boolean result = null;
    if (this.config != null) {
      result = (Boolean) this.config.get(key);
    }

    if (result == null) {
      result = defaultValue;
    }

    return result;
  }

  protected Integer getIntegerConfig(String key, int defaultValue) {
    Integer result = null;
    if (this.config != null) {
      result = (Integer) this.config.get(key);
    }

    if (result == null) {
      result = defaultValue;
    }

    return result;
  }

  public abstract void configure(Map<String, Object> config);

}
