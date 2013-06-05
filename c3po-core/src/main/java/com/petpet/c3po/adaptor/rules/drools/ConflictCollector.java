package com.petpet.c3po.adaptor.rules.drools;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.petpet.c3po.dao.MetadataUtil;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.MetadataRecord;
import com.petpet.c3po.datamodel.Source;

public class ConflictCollector {

  private Map<String, Set<Element>> conflicts = new ConcurrentHashMap<String, Set<Element>>();
  private MetadataUtil metadataUtil;

  public ConflictCollector(MetadataUtil metadataUtil) {
    super();
    this.metadataUtil = metadataUtil;
  }

  public void addConflict(String propertyName, Element element) {
    Set<Element> set = this.conflicts.get(propertyName);
    if (set == null) {
      set = new HashSet<Element>();
      this.conflicts.put(propertyName, set);
    }
    set.add(element);
  }

  public void printStatistics(PrintStream output) {
    output.println("======================================");
    output.println("Remaining Conflict Statistics:");

    for (Entry<String, Set<Element>> conflictEntry : this.conflicts.entrySet()) {
      output.println("--------------------------------------");
      String property = conflictEntry.getKey();
      output.println("property: '" + property + "'");
      for (Element element : conflictEntry.getValue()) {
        output.println("----element: " + element.getUid());
        for (MetadataRecord metadataRecord : element.getMetadata()) {
          if (metadataRecord.getProperty().getId().equals(property)) {
            output.println("    " + metadataRecord.getValue());
            for (String sourceID : metadataRecord.getSources()) {
              Source source = this.metadataUtil.resolveSourceID(sourceID);
              output.println("        Source: " + source.getName() + " "
                  + source.getVersion() + " [" + source.getId() + "]");
            }
          }
        }
      }
    }

    output.println("======================================");
  }
}
