package com.petpet.c3po.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.utils.Configurator;

public class MetadataUtil {

  private static Cache cache;
  private static Map<String, Source> sourceIDCache;

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
    
    if(sourceIDCache == null || cache == null) {
      initialize();
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
  
  private static void initialize() {
    cache = Configurator.getDefaultConfigurator().getPersistence().getCache();
    sourceIDCache = new ConcurrentHashMap<String, Source>();
  }
}