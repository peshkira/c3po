package com.petpet.collpro.tree;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import com.petpet.collpro.datamodel.Element;

public class ElementNode extends NamedNode implements TreeNode {

	private static final long serialVersionUID = 8824766565662286821L;
	
	private ElementFilterNode parent;

	private Element element;

	public ElementNode() {

	}

	public ElementNode(ElementFilterNode parent, Element e) {
		this();
		this.setType("element");
		this.setElement(e);
		this.setName(e.getName());
		this.setParent(parent);
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	@Override
	public Enumeration children() {
		return new Enumeration<TreeNode>() {

			public boolean hasMoreElements() {
				return false;
			}

			public TreeNode nextElement() {
				return null;
			}
		};
	}

	@Override
	public boolean getAllowsChildren() {
		return false;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return null;
	}

	@Override
	public int getChildCount() {
		return 0;
	}

	@Override
	public int getIndex(TreeNode node) {
		return 0;
	}

	@Override
	public ElementFilterNode getParent() {
		return this.parent;
	}
	
	public void setParent(ElementFilterNode parent) {
		this.parent = parent;
		this.parent.getChildren().add(this);
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

}
