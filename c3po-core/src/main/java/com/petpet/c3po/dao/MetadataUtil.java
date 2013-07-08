package com.petpet.c3po.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;

public class MetadataUtil {

  private Cache cache;
  private Map<String, Source> sourceIDCache;

  public MetadataUtil(Cache cache) {
    super();
    this.cache = cache;
    this.sourceIDCache = new ConcurrentHashMap<String, Source>();
  }

  public boolean haveSameSources(MetadataRecord record1, MetadataRecord record2) {
    return record1.getSources().containsAll(record2.getSources())
        && record2.getSources().containsAll(record1.getSources());
  }

  public boolean isFromTool(MetadataRecord record, String toolName) {
    for (String sourceID : record.getSources()) {
      Source source = this.resolveSourceID(sourceID);
      if (source.getName().equals(toolName)) {
        return true;
      }
    }
    return false;
  }

  public boolean isFromTool(MetadataRecord record, String toolName,
      String toolVersion) {
    for (String sourceID : record.getSources()) {
      Source source = this.resolveSourceID(sourceID);
      if (source.getName().equals(toolName)
          && source.getVersion().equals(toolVersion)) {
        return true;
      }
    }
    return false;
  }

  public Source resolveSourceID(String sourceID) {

    Source source = this.sourceIDCache.get(sourceID);

    if (source == null) {
      source = this.cache.getSource(sourceID);
      this.sourceIDCache.put(sourceID, source);
    }

    return source;
  }
}