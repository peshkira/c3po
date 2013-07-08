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

import com.petpet.c3po.utils.Configurator;

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

  /**
   * TODO: this could be done via {@link Configurator}
   */
  private static final Character CSV_SEPERATOR = ';';

  /**
   * TODO: this could be done via {@link Configurator}
   */
  private static final Character CSV_LIMITER = '\"';

  private static RuleActivationListener SINGLETON = null;

  private Map<Rule, Integer> activations;
  private Collection<KnowledgePackage> packages;

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

  /**
   * Initialize the {@link RuleActivationListener} by telling it what packages
   * and rules are present in the knowledge base.
   * 
   * @param knowledgePackages
   */
  public void initialize(Collection<KnowledgePackage> knowledgePackages) {
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
    this.activations.put(firedRule, counter+1);

  }

  /**
   * Print, how often every rule was activated to the provided {@link PrintStream}.
   * 
   * @param output
   * @param csvStyle
   */
  public void printStatistics(PrintStream output, boolean csvStyle) {
    if (!csvStyle) {
      output.println("======================================");
      output.println("Invoked Rules Statistics");
    } else {
      output.println("Invoked Rules Statistics");
      output.println(CSVParser.prepareLine(CSV_SEPERATOR, CSV_LIMITER, "Package Name", "Rule Name", "Activations"));
    }

    for (KnowledgePackage rulesPackage : this.packages) {
      if (!csvStyle) {
        output.println("--------------------------------------");
        output.println("Package: '" + rulesPackage.getName() + "'");
      }
      for (Rule rule : rulesPackage.getRules()) {
        rule = this.unwrapRule(rule);
        if (!csvStyle) {
          output.println("* '" + rule.getName() + "'");
          output.println("  activations:" + this.activations.get(rule));
        } else {
          output.println(CSVParser.prepareLine(CSV_SEPERATOR, CSV_LIMITER, rulesPackage.getName(), rule.getName(),
              String.valueOf(this.activations.get(rule))));
        }
      }
    }

    if (!csvStyle) {
      output.println("======================================");
    }
  }

  private Rule unwrapRule(Rule rule) {
    if (rule instanceof RuleImpl) {
      rule = ((RuleImpl) rule).getRule();
    }
    return rule;
  }

}
