package com.petpet.c3po.datamodel;

public class LogEntry {

	public static enum ChangeType {
		IGNORED,
		UPDATED,
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
}
