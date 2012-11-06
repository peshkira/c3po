package com.petpet.c3po.adaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.petpet.c3po.adaptor.rules.PostProcessingRule;
import com.petpet.c3po.adaptor.rules.PreProcessingRule;
import com.petpet.c3po.adaptor.rules.ProcessingRule;
import com.petpet.c3po.controller.Controller;

public abstract class AbstractAdaptor implements Runnable {

  public static final String UNKNOWN_COLLECTION_ID = "unknown";

  private Controller controller;

  private Map<String, Object> config;

  private List<ProcessingRule> rules;

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

  protected List<ProcessingRule> getRules() {
    Collections.sort(this.rules, new Comparator<ProcessingRule>() {

      // sorts from descending
      @Override
      public int compare(ProcessingRule r1, ProcessingRule r2) {
        int first = this.fixPriority(r2.getPriority());
        int second = this.fixPriority(r1.getPriority());
        return new Integer(first).compareTo(new Integer(second));
      }

      private int fixPriority(int prio) {
        if (prio < 0)
          return 0;

        if (prio > 1000)
          return 1000;

        return prio;
      }

    });

    return rules;
  }

  protected List<PreProcessingRule> getPreProcessingRules() {
    List<ProcessingRule> all = this.getRules();
    List<PreProcessingRule> prerules = new ArrayList<PreProcessingRule>();

    for (ProcessingRule rule : all) {
      if (rule instanceof PreProcessingRule) {
        prerules.add((PreProcessingRule) rule);
      }
    }

    return prerules;
  }
  
  protected List<PostProcessingRule> getPostProcessingRules() {
    List<ProcessingRule> all = this.getRules();
    List<PostProcessingRule> prerules = new ArrayList<PostProcessingRule>();

    for (ProcessingRule rule : all) {
      if (rule instanceof PostProcessingRule) {
        prerules.add((PostProcessingRule) rule);
      }
    }

    return prerules;
  }

  public void setRules(List<ProcessingRule> rules) {
    this.rules = rules;
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
