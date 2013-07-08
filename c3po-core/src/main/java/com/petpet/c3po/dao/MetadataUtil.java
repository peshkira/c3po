package com.petpet.c3po.dao;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.petpet.c3po.adaptor.rules.drools.MetadataSorter;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.utils.Configurator;

/**
 * <p>
 * This is a static helper class that allows some simple tests on
 * MetadataRecords to keep the code in rule definitions more readable.
 * </p>
 * <p>
 * It further enables the reverse resolving of {@link Source} objects from their
 * IDs by wrapping around the {@link Cache#getSource(String)} method and
 * provides its own simple caching for those values to reduce database queries.
 * This method could be avoided if the {@link MetadataRecord} would hold the
 * complete {@link Source} objects instead of just the IDs.
 * </p>
 */
public class MetadataUtil {

  private static Cache cache;
  private static Map<String, Source> sourceIDCache;
  private static Comparator<MetadataRecord> sorter;

  private MetadataUtil() {
  }

  public static boolean haveSameSources(MetadataRecord record1, MetadataRecord record2) {
    return record1.getSources().containsAll(record2.getSources())
        && record2.getSources().containsAll(record1.getSources());
  }

  public static boolean isFromTool(MetadataRecord record, String toolName) {
    for (String sourceID : record.getSources()) {
      Source source = resolveSourceID(sourceID);
      if (source.getName().equals(toolName)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isFromTool(MetadataRecord record, String toolName,
      String toolVersion) {
    for (String sourceID : record.getSources()) {
      Source source = resolveSourceID(sourceID);
      if (source.getName().equals(toolName)
          && source.getVersion().equals(toolVersion)) {
        return true;
      }
    }
    return false;
  }

  public static Source resolveSourceID(String sourceID) {

    if (sourceIDCache == null || cache == null) {
      cache = Configurator.getDefaultConfigurator().getPersistence().getCache();
      sourceIDCache = new ConcurrentHashMap<String, Source>();
    }

    Source source = sourceIDCache.get(sourceID);

    if (source == null) {
      synchronized (sourceIDCache) {
        source = cache.getSource(sourceID);
        sourceIDCache.put(sourceID, source);
      }
    }

    return source;
  }
  
  public static Comparator<MetadataRecord> getMetadataSorter() {
    if (sorter == null) {

      sorter = new MetadataSorter
          ();
    }
    return sorter;
  }

}