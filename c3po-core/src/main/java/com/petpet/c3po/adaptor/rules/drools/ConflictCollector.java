package com.petpet.c3po.adaptor.rules.drools;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.dao.MetadataUtil;

public class ConflictCollector {

  private Map<String, Set<Element>> conflicts = Collections
      .synchronizedMap(new TreeMap<String, Set<Element>>());

  public ConflictCollector() {
    super();
  }

  public void addConflict(String propertyName, Element element) {

    Set<Element> set = this.conflicts.get(propertyName);
    if (set == null) {
      set = new HashSet<Element>();
      this.conflicts.put(propertyName, set);
    }
    set.add(element);
  }

  public void printAccumulatedStatistics(PrintStream output, boolean csvStyle) {
    if (!csvStyle) {
      output.println("======================================");
    }

    output.println("Remaining Conflict Statistics:");

    synchronized (this.conflicts) {
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
          output.println(property + ";" + conflictEntry.getValue().size());
        }
      }
    }
    if (!csvStyle) {
      output.println("======================================");
    }
  }

  public void printStatistics(PrintStream output) {
    output.println("======================================");
    output.println("Remaining Conflict Statistics:");

    synchronized (this.conflicts) {
      for (Entry<String, Set<Element>> conflictEntry : this.conflicts
          .entrySet()) {
        output.println("--------------------------------------");
        String property = conflictEntry.getKey();
        output.println("property: '" + property + "'");
        for (Element element : conflictEntry.getValue()) {
          output.println("----element: " + element.getUid());
          for (MetadataRecord metadataRecord : element.getMetadata()) {
            if (metadataRecord.getProperty().getId().equals(property)) {
              output.println("    " + metadataRecord.getValue());
              for (String sourceID : metadataRecord.getSources()) {
                Source source = MetadataUtil.resolveSourceID(sourceID);
                output.println("        Source: " + source.getName() + " "
                    + source.getVersion() + " [" + source.getId() + "]");
              }
            }
          }
        }
      }
    }
    output.println("======================================");
  }
}
