package com.petpet.c3po.adaptor.rules.drools;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.petpet.c3po.adaptor.rules.DroolsConflictResolutionProcessingRule;
import com.petpet.c3po.api.model.Element;

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

  private static ConflictCollector SINGLETON = null;

  private Map<String, Set<Element>> conflicts = Collections
      .synchronizedMap(new TreeMap<String, Set<Element>>());

  private ConflictCollector() {
    super();
  }

  /**
   * @return The singleton instance of {@link ConflictCollector}.
   */
  public static synchronized ConflictCollector getInstance() {
    if (SINGLETON == null) {
      SINGLETON = new ConflictCollector();
    }
    return SINGLETON;
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

  public void clearConflicts() {
    synchronized (this.conflicts) {
      this.conflicts.clear();
    }
  }

  public Map<String, Set<Element>> getConflicts() {

    Map<String, Set<Element>> copyOfConflicts = new LinkedHashMap<String, Set<Element>>(
        this.conflicts.size());

    synchronized (this.conflicts) {
      for (Entry<String, Set<Element>> entry : this.conflicts.entrySet()) {
        Set<Element> set = new LinkedHashSet<Element>(entry.getValue());
        copyOfConflicts.put(entry.getKey(), set);
      }
    }

    return copyOfConflicts;
  }
}
