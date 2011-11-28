package com.petpet.c3po.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.google.common.collect.Iterators;

public class FilterNode extends NamedNode implements TreeNode {

	private static final long serialVersionUID = -9091366224987715809L;
	
	private List<ElementFilterNode> secondOrderFilters;
	
	public FilterNode() {
		this.secondOrderFilters = new ArrayList<ElementFilterNode>();
	}
	
	public FilterNode(String name) {
		this();
		this.setType("filter");
		this.setName(name);
	}

	@Override
	public Enumeration children() {
		return Iterators.asEnumeration(this.secondOrderFilters.iterator());
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return this.secondOrderFilters.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return this.secondOrderFilters.size();
	}

	@Override
	public int getIndex(TreeNode node) {
		return this.secondOrderFilters.indexOf(node);
	}

	@Override
	public TreeNode getParent() {
		return null;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}
	
	public List<ElementFilterNode> getChildren() {
		return this.secondOrderFilters;
	}

}
