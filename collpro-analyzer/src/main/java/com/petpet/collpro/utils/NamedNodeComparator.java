package com.petpet.collpro.utils;

import java.util.Comparator;

import com.petpet.collpro.tree.NamedNode;

public class NamedNodeComparator implements Comparator<NamedNode> {

	@Override
	public int compare(NamedNode n1, NamedNode n2) {
		return n1.getName().compareTo(n2.getName());
	}

}
