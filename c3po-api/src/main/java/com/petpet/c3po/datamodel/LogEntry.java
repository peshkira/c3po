package com.petpet.c3po.datamodel;

/**
 * User: phoenix
 * Date: 31.05.13
 * Time: 20:06
 */
public class LogEntry {

	public static enum Change {
		IGNORED,
		UPDATED,
		REMOVED,
		MERGED
	}

	private String metadataProperty;
	private String metadataValueOld;
	private Change change;

	public LogEntry(String metadataProperty, String metadataValueOld, Change change) {
		this.metadataProperty = metadataProperty;
		this.metadataValueOld = metadataValueOld;
		this.change = change;
	}

	public String getMetadataProperty() {
		return metadataProperty;
	}

	public String getMetadataValueOld() {
		return metadataValueOld;
	}

	public Change getChange() {
		return change;
	}
}
