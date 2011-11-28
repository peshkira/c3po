package com.petpet.collpro.tree;

import java.io.Serializable;
import java.util.List;

public class NamedNode implements Serializable {

	private static final long serialVersionUID = -2487631848992933774L;

	private String name;
	
	private String type;
	
	private List<String> distinctProperties;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getDistinctProperties() {
		System.out.println("super call");
		return distinctProperties;
	}

	public void setDistinctProperties(List<String> distinctProperties) {
		this.distinctProperties = distinctProperties;
	}
	
	

}
