package com.petpet.c3po.api.adaptor;

public interface ProcessingRule {

  /**
   * A priority between 0 and 1000 in the case of more rules, where 0 is the
   * least important and 1000 is the most important rule. If the set priority is
   * smaller than 0, then 0 will be used, if it is larger than 1000, then 1000
   * will be used.
   * 
   * @return the priority of this rule.
   */
  int getPriority();
}
