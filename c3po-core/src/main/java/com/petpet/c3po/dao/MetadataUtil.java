package com.petpet.c3po.dao;

import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.datamodel.MetadataRecord;
import com.petpet.c3po.datamodel.Source;

public class MetadataUtil {

  public static boolean isFromTool(Cache cache, MetadataRecord record, String toolName) {
    for (String sourceID : record.getSources()) {
      Source source = cache.getSource(sourceID);
      if (source.getName().equals(toolName)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isFromTool(Cache cache, MetadataRecord record, String toolName, String toolVersion) {
    for (String sourceID : record.getSources()) {
      Source source = cache.getSource(sourceID);
      if (source.getName().equals(toolName) && source.getVersion().equals(toolVersion)) {
        return true;
      }
    }
    return false;
  }

  public static boolean haveSameSources(MetadataRecord record1, MetadataRecord record2) {
    return (record1.getSources().containsAll(record2.getSources()) && record2.getSources().containsAll(
        record1.getSources()));
  }
}