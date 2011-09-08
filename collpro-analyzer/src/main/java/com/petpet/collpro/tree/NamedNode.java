package com.petpet.collpro.tree;

import java.io.Serializable;

public class NamedNode implements Serializable {

	private static final long serialVersionUID = -2487631848992933774L;

	private String name;
	
	private String type;

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

}
