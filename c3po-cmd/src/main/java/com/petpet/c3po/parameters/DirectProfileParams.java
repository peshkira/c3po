package com.petpet.c3po.parameters;

import com.beust.jcommander.Parameter;
import com.petpet.c3po.parameters.validation.EmptyStringValidator;

public class DirectProfileParams implements Params {

	/**
	 * The collection that will be profiled - required. Supports '-c' and
	 * '--collection'.
	 */
	@Parameter(names = { "-c", "--collection" }, validateValueWith = EmptyStringValidator.class, required = true, description = "The name of the collection")
	private String collection;

	/**
	 * The output directory location for the generated profile. The default
	 * value is the working directory. Supports '-o' and '--outputdir'.
	 */
	@Parameter(names = { "-o", "--outputdir" }, description = "The output directory where the profile will be stored")
	private String olocation = "";

	/**
	 * The input directory, where the meta data is stored - required. Supports
	 * '-i' and '--inputdir'.
	 */
	@Parameter(names = { "-i", "--inputdir" }, description = "The input directory where the meta data is stored", required = true)
	private String ilocation;

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getInputLocation() {
		return ilocation;
	}

	public void setInputLocation(String location) {
		this.ilocation = location;
	}
	
	public String getOutnputLocation() {
		return olocation;
	}

	public void setOutnputLocation(String location) {
		this.olocation = location;
	}

}
