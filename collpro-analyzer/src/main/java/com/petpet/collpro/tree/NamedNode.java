package com.petpet.collpro.tree;

import java.io.Serializable;

public class NamedNode implements Serializable {

	private static final long serialVersionUID = -2487631848992933774L;

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}

}
