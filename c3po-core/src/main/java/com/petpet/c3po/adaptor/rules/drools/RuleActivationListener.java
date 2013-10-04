package com.petpet.c3po.adaptor.rules.drools;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.definitions.rule.impl.RuleImpl;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;

/**
 * <p>
 * This is a singleton object that collects data about activation of rules to
 * get statistical information about them.
 * </p>
 * <p>
 * It is able to report statistics about the conflicts found either human
 * readable or in a CSV style (separated by semicolon)
 * </p>
 */
public class RuleActivationListener extends DefaultAgendaEventListener {

  private static RuleActivationListener SINGLETON = null;

  private Map<KnowledgePackage, Map<Rule, Integer>> activations;

  private HashMap<String, KnowledgePackage> knowledgePackages;

  private RuleActivationListener() {
  }

  /**
   * @return The singleton instance of {@link RuleActivationListener}.
   */
  public static synchronized RuleActivationListener getInstance() {
    if (SINGLETON == null) {
      SINGLETON = new RuleActivationListener();
    }
    return SINGLETON;
  }

  private static Rule unwrapRule(Rule rule) {
    if (rule instanceof RuleImpl) {
      rule = ((RuleImpl) rule).getRule();
    }
    return rule;
  }

  @Override
  public void afterActivationFired(AfterActivationFiredEvent event) {
    Rule firedRule = event.getActivation().getRule();

    KnowledgePackage knowledgePackage = this.knowledgePackages.get(firedRule
        .getPackageName());

    synchronized (this.activations) {
      Map<Rule, Integer> rulesActivations = this.activations
          .get(knowledgePackage);
      Integer counter = rulesActivations.get(firedRule);
      counter++;
      rulesActivations.put(firedRule, counter + 1);
    }
  }

  public Map<KnowledgePackage, Map<Rule, Integer>> getActivations() {

    Map<KnowledgePackage, Map<Rule, Integer>> activationsCopy = new LinkedHashMap<KnowledgePackage, Map<Rule, Integer>>(
        this.activations.size());

    synchronized (this.activations) {
      for (Entry<KnowledgePackage, Map<Rule, Integer>> packageEntry : this.activations
          .entrySet()) {
        Map<Rule, Integer> packageActivationsCopy = new LinkedHashMap<Rule, Integer>(
            packageEntry.getValue());
        activationsCopy.put(packageEntry.getKey(), packageActivationsCopy);
      }
    }

    return activationsCopy;
  }

  /**
   * Initialize the {@link RuleActivationListener} by telling it what packages
   * and rules are present in the knowledge base.
   * 
   * @param knowledgePackages
   */
  public void initialize(Collection<KnowledgePackage> knowledgePackages) {
    this.activations = Collections
        .synchronizedMap(new LinkedHashMap<KnowledgePackage, Map<Rule, Integer>>());
    this.knowledgePackages = new HashMap<String, KnowledgePackage>(
        knowledgePackages.size());

    for (KnowledgePackage knowledgePackage : knowledgePackages) {
      Map<Rule, Integer> ruleActivations = Collections
          .synchronizedMap(new LinkedHashMap<Rule, Integer>());
      this.knowledgePackages.put(knowledgePackage.getName(), knowledgePackage);

      this.activations.put(knowledgePackage, ruleActivations);
      for (Rule rule : knowledgePackage.getRules()) {
        // get the REAL rule object from the wrapper
        rule = unwrapRule(rule);

        ruleActivations.put(rule, 0);
      }
    }
  }

}
