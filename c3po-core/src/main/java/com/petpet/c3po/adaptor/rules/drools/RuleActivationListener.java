package com.petpet.c3po.adaptor.rules.drools;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.definitions.rule.impl.RuleImpl;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;

public class RuleActivationListener extends DefaultAgendaEventListener {

  private Map<Rule, Integer> activations;
  private Collection<KnowledgePackage> packages;

  public RuleActivationListener(Collection<KnowledgePackage> knowledgePackages) {
    this.packages = knowledgePackages;
    this.activations = new HashMap<Rule, Integer>();

    for (KnowledgePackage knowledgePackage : knowledgePackages) {
      for (Rule rule : knowledgePackage.getRules()) {
        // get the REAL rule object from the wrapper
        rule = this.unwrapRule(rule);

        this.activations.put(rule, 0);
      }
    }
  }

  @Override
  public synchronized void afterActivationFired(AfterActivationFiredEvent event) {
    Rule firedRule = event.getActivation().getRule();

    Integer counter = this.activations.get(firedRule);
    counter++;
    this.activations.put(firedRule, counter);

  }

  public void printStatistics(PrintStream output, boolean csvStyle) {
    if (!csvStyle) {
      output.println("======================================");
    }
    output.println("Drools Conflict Resolution Statistics:");

    for (KnowledgePackage rulesPackage : this.packages) {
      if (!csvStyle) {
        output.println("--------------------------------------");
        output.println("Package: '" + rulesPackage.getName() + "'");
      }
      for (Rule rule : rulesPackage.getRules()) {
        rule = this.unwrapRule(rule);
        if (!csvStyle) {
          output.println("* '" + rule.getName() + "'");
          output.println("  #:" + this.activations.get(rule));
        } else {
          output.println(rule.getName() + ";" + this.activations.get(rule));
        }

      }
    }

    if (!csvStyle) {
      output.println("======================================");
    }
  }

  public Rule unwrapRule(Rule rule) {
    if (rule instanceof RuleImpl) {
      rule = ((RuleImpl) rule).getRule();
    }
    return rule;
  }

}