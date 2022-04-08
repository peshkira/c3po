package com.petpet.c3po.api.model.helper;

import java.io.Serializable;

/**
 * <p>A LogEntry holds information about changes that were applied to a
 * {@link MetadataRecord}, based on the execution of a rule. Although the rule
 * is just referenced by its name and can therefore be set arbitrarily, it is
 * meant to hold the name of a rule that is part of the drool-powered
 * postprocessing.
 * </p>
 * <p>Everytime a {@link MetadataRecord} is modified in some way, a
 * {@link LogEntry} must be created and added to it to allow traceability of
 * changes.
 * </p>
 */
public class LogEntry implements Serializable {

  public static enum ChangeType {
    IGNORED,
    UPDATED,
    ADDED,
    MERGED
  }

  private String metadataProperty;
  private String metadataValueOld;
  private ChangeType changeType;
  private String ruleName;

  public LogEntry(String metadataProperty, String metadataValueOld, ChangeType changeType, String ruleName) {
    this.metadataProperty = metadataProperty;
    this.metadataValueOld = metadataValueOld;
    this.changeType = changeType;
    this.ruleName = ruleName;
  }

  public String getMetadataProperty() {
    return metadataProperty;
    }

  public String getMetadataValueOld() {
    return metadataValueOld;
  }

  public ChangeType getChangeType() {
    return changeType;
  }


  public String getRuleName() {
    return ruleName;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("LogEntry [property=");
    builder.append(this.metadataProperty);
    builder.append(", by rule: ");
    builder.append(this.ruleName);
    builder.append(", old value=");
    builder.append(this.metadataValueOld);
    builder.append(", changeType=");
    builder.append(this.changeType);
    builder.append("]\n\n");
    return builder.toString();
  }

}
