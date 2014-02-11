package com.petpet.c3po.adaptor.rules.drools;

import java.util.List;

import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.dao.MetadataUtil;

/**
 * <p>
 * This class is used to collect log messages inside the drools rule execution.
 * Because multiple sessions can run in parallel, a linear output would be
 * almost unreadable. Therefore the {@link LogCollector} saves all log messages
 * and returns them when processing is finished. This allows to echo the output
 * at once without interference by simultaneous threads.
 * </p>
 * <p>
 * Futher, it provides some standard logging methods that helps keepint the code
 * cleaner.
 * </p>
 * TODO: maybe use java.util.logging.Level or org.apache.commons.logging.Log to
 * follow some framework here. But in fact, the configuration of this should be 
 * done via CLI parameters.
 */
public class LogCollector {

  public static int TRACE = 0;
  public static int DEBUG = 1;
  public static int INFO = 2;

  private StringBuilder stringBuilder = new StringBuilder();

  private int minimumLoglevel;

  public LogCollector(int minimumLoglevel) {
    super();
    this.minimumLoglevel = minimumLoglevel;
  }

  /**
   * Add an arbitrary String to the log with the given log level. Automatically
   * terminated with a newline character.
   * 
   * @param loglevel
   * @param text
   */
  public synchronized void log(int loglevel, String text) {
    if (this.minimumLoglevel <= loglevel) {
      this.stringBuilder.append(text);
      this.stringBuilder.append("\n");
    }
  }

  /**
   * Log a {@link MetadataRecord}s value and sources with the given log level.
   * 
   * @see LogCollector#logMetadataRecordValue(int, MetadataRecord)
   * @see LogCollector#logMetadataRecordSources(int, List)
   * 
   * @param loglevel
   * @param record
   */
  public synchronized void logMetadataRecord(int loglevel, MetadataRecord record) {
    this.logMetadataRecordValue(loglevel, record);
    List<String> sourceIDs = record.getSources();
    this.logMetadataRecordSources(loglevel, sourceIDs);
  }

  /**
   * Log a {@link MetadataRecord}s sources with the given log level.
   * 
   * @param loglevel
   * @param sourceIDs
   */
  public synchronized void logMetadataRecordSources(int loglevel, List<String> sourceIDs) {
    for (String sourceID : sourceIDs) {
      Source source = MetadataUtil.resolveSourceID(sourceID);
      this.log(loglevel,
          "        Source: " + source.getName() + " " + source.getVersion()
              + " [" + source.getId() + "]");
    }
  }

  /**
   * Log a {@link MetadataRecord}s value with the given log level.
   * 
   * @param loglevel
   * @param record
   */
  public synchronized void logMetadataRecordValue(int loglevel, MetadataRecord record) {
    this.log(loglevel,
        "    " + record.getProperty().getId() + " : \'" + record.getValue()
            + "\'");
  }

  /**
   * Clear the log buffer and retrieve the logged messages.
   * 
   * @return The whole log of this {@link LogCollector}.
   */
  public synchronized String reset() {
    String string = this.stringBuilder.toString();
    this.stringBuilder.setLength(0);
    return string;
  }
}
