package com.petpet.c3po.datamodel;

public class LogEntry {

	private String metadataProperty;

	private String metadataValueOld;
	private ChangeType changeType;
	private String ruleName;

	public LogEntry(String metadataProperty, String metadataValueOld,
			ChangeType changeType, String ruleName) {
		this.metadataProperty = metadataProperty;
		this.metadataValueOld = metadataValueOld;
		this.changeType = changeType;
		this.ruleName = ruleName;
	}

	public ChangeType getChangeType() {
		return this.changeType;
	}

	public String getMetadataProperty() {
		return this.metadataProperty;
	}

	public String getMetadataValueOld() {
		return this.metadataValueOld;
	}

	public String getRuleName() {
		return this.ruleName;
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

	public static enum ChangeType {
		IGNORED, UPDATED, ADDED, MERGED
	}
}
