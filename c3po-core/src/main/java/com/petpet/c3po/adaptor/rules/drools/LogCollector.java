package com.petpet.c3po.adaptor.rules.drools;

import java.util.List;

import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.datamodel.MetadataRecord;
import com.petpet.c3po.datamodel.Source;

public class LogCollector {

  private StringBuilder stringBuilder = new StringBuilder();

  private Cache cache;

  public LogCollector(Cache cache) {
    super();
    this.cache = cache;
  }

  public synchronized void debug(String text) {
    this.stringBuilder.append(text);
    this.stringBuilder.append("\n");
  }

  public synchronized void debugMetadataRecord(MetadataRecord record) {
    this.debugMetadataRecordValue(record);
    List<String> sourceIDs = record.getSources();
    this.debugMetadataRecordSources(sourceIDs);
  }

  public void debugMetadataRecordSources(List<String> sourceIDs) {
    for (String sourceID : sourceIDs) {
      Source source = this.cache.getSource(sourceID);
      this.log("        Source: " + source.getName() + " "
          + source.getVersion() + " [" + source.getId() + "]");
    }
  }

  public void debugMetadataRecordValue(MetadataRecord record) {
    this.debug("    " + record.getProperty().getId() + " : \'"
        + record.getValue() + "\'");
  }

  public synchronized void log(String text) {
    this.stringBuilder.append(text);
    this.stringBuilder.append("\n");
  }

  public synchronized String reset() {
    String string = this.stringBuilder.toString();
    this.stringBuilder.setLength(0);
    return string;

  }
}
