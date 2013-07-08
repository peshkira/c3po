package com.petpet.c3po.adaptor.rules.drools;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.petpet.c3po.adaptor.rules.DroolsConflictResolutionProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.dao.MetadataUtil;
import com.petpet.c3po.utils.Configurator;

/**
 * <p>
 * This is a singleton object that collects data about conflicts that remain
 * after conflict resolution by {@link DroolsConflictResolutionProcessingRule}
 * is done.
 * </p>
 * <p>
 * It is able to report statistics about the conflicts found either human
 * readable or in a CSV style (separated by semicolon)
 * </p>
 */
public class ConflictCollector {

  /**
   * TODO: this could be done via {@link Configurator}
   */
  private static final Character CSV_SEPERATOR = ';';

  /**
   * TODO: this could be done via {@link Configurator}
   */
  private static final Character CSV_LIMITER = '\"';

  private static ConflictCollector SINGLETON = null;

  private Map<String, Set<Element>> conflicts = Collections
      .synchronizedMap(new TreeMap<String, Set<Element>>());

  /**
   * @return The singleton instance of {@link ConflictCollector}.
   */
  public static synchronized ConflictCollector getInstance() {
    if (SINGLETON == null) {
      SINGLETON = new ConflictCollector();
    }
    return SINGLETON;
  }

  private ConflictCollector() {
    super();
  }

  /**
   * Report a conflict of the given element on the given property.
   * 
   * @param propertyName
   * @param element
   */
  public void addConflict(String propertyName, Element element) {
    Set<Element> set = this.conflicts.get(propertyName);
    if (set == null) {
      set = new HashSet<Element>();
      this.conflicts.put(propertyName, set);
    }
    set.add(element);
  }

  /**
   * Print a report of how many conflicts are found per property name to the
   * provided {@link PrintStream}. A conflict is counted per conflicting
   * property, not per conflicting value!
   * 
   * @param output
   * @param csvStyle
   */
  public void printAccumulatedStatistics(PrintStream output, boolean csvStyle) {
    if (!csvStyle) {
      output.println("======================================");
      output.println("Remaining Conflict Statistics (accumulated):");
    } else {
      output.println("Remaining Conflict Statistics (accumulated):");
      output.println(CSVParser.prepareLine(CSV_SEPERATOR, CSV_LIMITER, "Property Name",
          "Conflicting Elements"));
    }

    synchronized (conflicts) {
      for (Entry<String, Set<Element>> conflictEntry : this.conflicts
          .entrySet()) {
        if (!csvStyle) {
          output.println("--------------------------------------");
        }
        String property = conflictEntry.getKey();
        if (!csvStyle) {
          output.println("property: '" + property + "'");
          output.println("    " + conflictEntry.getValue().size()
              + " elements in conflict");
        } else {
          output.println(CSVParser.prepareLine(CSV_SEPERATOR, CSV_LIMITER, property,
              String.valueOf(conflictEntry.getValue().size())));
        }
      }
    }

    if (!csvStyle) {
      output.println("======================================");
    }
  }

  /**
   * Print every single conflicting value, grouped by property with the affected
   * element, value and sources to the provided {@link PrintStream}.
   * 
   * @param output
   * @param csvStyle
   */
  public void printStatistics(PrintStream output, boolean csvStyle) {
    if (!csvStyle) {
      output.println("======================================");
      output.println("Remaining Conflict Statistics (detailed):");
    } else {
      output.println("Remaining Conflict Statistics (detailed):");
      output.println(CSVParser.prepareLine(CSV_SEPERATOR, CSV_LIMITER, "Property Name",
          "Element", "Value", "Source Name", "Source Version", "Source ID"));
    }

    synchronized (conflicts) {

      for (Entry<String, Set<Element>> conflictEntry : this.conflicts
          .entrySet()) {
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
            if (metadataRecord.getProperty().getId().equals(property)) {
              if (!csvStyle) {
                output.println("    " + metadataRecord.getValue());
              }
              for (String sourceID : metadataRecord.getSources()) {
                Source source = MetadataUtil.resolveSourceID(sourceID);
                if (!csvStyle) {
                  output.println("        Source: " + source.getName() + " "
                      + source.getVersion() + " [" + source.getId() + "]");
                } else {

                  output.println(CSVParser.prepareLine(CSV_SEPERATOR, CSV_LIMITER, property, element.getUid(),
                      metadataRecord.getValue(), source.getName(), source.getVersion(), source.getId()));
                }
              }
            }
          }
        }
      }
    }
    if (!csvStyle) {
      output.println("======================================");
    }
  }

}
