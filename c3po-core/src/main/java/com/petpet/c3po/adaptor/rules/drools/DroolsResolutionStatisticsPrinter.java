package com.petpet.c3po.adaptor.rules.drools;

import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;

import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.dao.MetadataUtil;
import com.petpet.c3po.utils.Configurator;

public class DroolsResolutionStatisticsPrinter {

  /**
   * TODO: this could be done via {@link Configurator}
   */
  private static final Character CSV_SEPERATOR = ';';

  /**
   * TODO: this could be done via {@link Configurator}
   */
  private static final Character CSV_LIMITER = '\"';

  /**
   * Print every single conflicting value, grouped by property with the affected
   * element, value and sources to the provided {@link PrintStream}.
   * 
   * @param output
   * @param csvStyle
   */
  public static void printConflicts(PrintStream output, boolean csvStyle) {
    if (!csvStyle) {
      output.println("======================================");
      output.println("Remaining Conflict Statistics (detailed):");
    } else {
      output.println("Remaining Conflict Statistics (detailed):");
      output.println(prepareLine("Property Name", "Element", "Value",
          "Source Name", "Source Version", "Source ID"));
    }

    Map<String, Set<Element>> conflicts = ConflictCollector.getInstance()
        .getConflicts();

    for (Entry<String, Set<Element>> conflictEntry : conflicts.entrySet()) {
      String property = conflictEntry.getKey();
      if (!csvStyle) {
        output.println("--------------------------------------");
        output.println("property: '" + property + "'");
      }
      for (Element element : conflictEntry.getValue()) {
        if (!csvStyle) {
          output.println("----element: " + element.getUid());
        }
        for (MetadataRecord metadataRecord : element.getMetadata()) {
         // if (metadataRecord.getProperty().getId().equals(property)) {
            if (!csvStyle) {
        //      output.println("    " + metadataRecord.getValue());
            }
            for (String sourceID : metadataRecord.getSources()) {
              Source source = MetadataUtil.resolveSourceID(sourceID);
              if (!csvStyle) {
                output.println("        Source: " + source.getName() + " "
                    + source.getVersion() + " [" + source.getId() + "]");
              } else {

                output.println(prepareLine(property, element.getUid(),
           //         metadataRecord.getValue(), source.getName(),
                    source.getVersion(), source.getId()));
              }
            }
          }
        }
      }
 //   }
   // if (!csvStyle) {
  //    output.println("======================================");
   // }

  }

  /**
   * Print a report of how many conflicts are found per property name to the
   * provided {@link PrintStream}. A conflict is counted per conflicting
   * property, not per conflicting value!
   * 
   * @param output
   * @param csvStyle
   */
  public static void printConflictsAccumulated(PrintStream output,
      boolean csvStyle) {
    if (!csvStyle) {
      output.println("======================================");
      output.println("Remaining Conflict Statistics (accumulated):");
    } else {
      output.println("Remaining Conflict Statistics (accumulated):");
      output.println(prepareLine("Property Name", "Conflicting Elements"));
    }

    Map<String, Set<Element>> conflicts = ConflictCollector.getInstance()
        .getConflicts();

    for (Entry<String, Set<Element>> conflictEntry : conflicts.entrySet()) {
      if (!csvStyle) {
        output.println("--------------------------------------");
      }
      String property = conflictEntry.getKey();
      if (!csvStyle) {
        output.println("property: '" + property + "'");
        output.println("    " + conflictEntry.getValue().size()
            + " elements in conflict");
      } else {
        output.println(prepareLine(property,
            String.valueOf(conflictEntry.getValue().size())));
      }
    }

    if (!csvStyle) {
      output.println("======================================");
    }

  }

  /**
   * Print, how often every rule was activated to the provided
   * {@link PrintStream}.
   * 
   * @param output
   * @param csvStyle
   */
  public static void printRuleActivation(PrintStream output, boolean csvStyle) {
    if (!csvStyle) {
      output.println("======================================");
      output.println("Invoked Rules Statistics");
    } else {
      output.println("Invoked Rules Statistics");
      output.println(prepareLine("Package Name", "Rule Name", "Activations"));
    }

    Map<KnowledgePackage, Map<Rule, Integer>> activations = RuleActivationListener
        .getInstance().getActivations();
    for (KnowledgePackage rulesPackage : activations.keySet()) {
      String packageName = rulesPackage.getName();
      if (!csvStyle) {
        output.println("--------------------------------------");
        output.println("Package: '" + packageName + "'");
      }
      for (Rule rule : activations.get(rulesPackage).keySet()) {
        String ruleName = rule.getName();
        Integer ruleCounter = activations.get(rulesPackage).get(rule);
        if (!csvStyle) {
          output.println("* '" + ruleName + "'");
          output.println("  activations:" + ruleCounter);
        } else {
          output.println(prepareLine(packageName, ruleName,
              String.valueOf(ruleCounter)));
        }
      }
    }

    if (!csvStyle) {
      output.println("======================================");
    }
  }

  /**
   * This is just a little helper to create CSV compatible output.
   */
  private static String prepareLine(String... values) {
    StringBuilder builder = new StringBuilder();

    for (String string : values) {
      builder.append(CSV_LIMITER);
      builder.append(string);
      builder.append(CSV_LIMITER);
      builder.append(CSV_SEPERATOR);
    }
    return builder.toString();
  }

}
