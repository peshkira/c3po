package com.petpet.c3po.api.model.helper;

public class LogEntry {

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
