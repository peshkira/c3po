package com.petpet.collpro.beans;

import static com.petpet.collpro.common.Constants.COLLECTION_DISTINCT_VALUES_IN_FILTERED;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.swing.tree.TreeNode;

import org.richfaces.component.UITree;
import org.richfaces.event.TreeSelectionChangeEvent;
import org.richfaces.event.TreeToggleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.tree.ElementFilterNode;
import com.petpet.collpro.tree.ElementNode;
import com.petpet.collpro.tree.FilterNode;
import com.petpet.collpro.tree.NamedNode;

@ManagedBean
@SessionScoped
public class TreeBean implements Serializable {

	private static final long serialVersionUID = -6484069020777687927L;

	private static final Logger LOG = LoggerFactory.getLogger(TreeBean.class);

	@PersistenceContext
	private EntityManager em;

	private NamedNode currentSelection = null;

	private DigitalCollection coll;

	private List<TreeNode> rootNodes;

	private List<String> knownPropertes = new ArrayList<String>();

	private String firstPFilter;

	private String secondPFilter;

	@PostConstruct
	public void init() {
		this.knownPropertes.addAll(this.em.createNamedQuery(
				"getAllPropertyNames", String.class).getResultList());

		this.coll = this.em.find(DigitalCollection.class, 1L); // FIXME
																// (selection by
																// user)
	}

	public void selectionChanged(TreeSelectionChangeEvent evt) {
		LOG.debug("tree selectionChanged...");
		// considering only single selection
		List<Object> selection = new ArrayList<Object>(evt.getNewSelection());
		Object currentSelectionKey = selection.get(0);
		UITree tree = (UITree) evt.getSource();
		Object storedKey = tree.getRowKey();
		tree.setRowKey(currentSelectionKey);
		NamedNode node = (NamedNode) tree.getRowData();
		LOG.info("TYPE IS: {}", node.getType());
		setCurrentSelection(node);
		tree.setRowKey(storedKey);
	}

	public void expansionChanged(TreeToggleEvent evt) {
		LOG.debug("tree expansion changed");
		if (evt.isExpanded()) {
			LOG.debug("tree node is expanded");
			UITree tree = (UITree) evt.getSource();
			TreeNode node = (TreeNode) tree.getRowData();

			if (node instanceof FilterNode) {
				FilterNode fn = (FilterNode) node;
				LOG.debug("node is FilterNode {}", fn.getName());
				
				for (ElementFilterNode efn : fn.getChildren()) {
					
					efn.getChildren().clear();

					List<Element> elements = this.em
							.createQuery(
									"SELECT val.element FROM Value val WHERE val.property.name = :pname2 AND val.value = :value2 AND val.element IN (SELECT v.element FROM Value v WHERE v.property.name = :pname1 AND v.value = :value1 AND v.element.collection = :coll) ORDER BY val.element.name",
									Element.class)
							.setParameter("pname1", this.firstPFilter)
							.setParameter("value1", fn.getName())
							.setParameter("pname2", this.secondPFilter)
							.setParameter("value2", efn.getName())
							.setParameter("coll", this.coll).getResultList();
					

					for (Element e : elements) {
						new ElementNode(efn, e);
					}
				}
			}
		}
	}

	public void createTree() {
		this.rootNodes = new ArrayList<TreeNode>();

		if (!this.isFilteringValid()) {
			return;
		}

		LOG.info("constructing tree...");
		long start = System.currentTimeMillis();
		this.filterTree();

		long end = System.currentTimeMillis();
		LOG.info("Tree constructed in: " + (end - start) + "ms");
	}

	private void filterTree() {
		List<String> filter1 = this.getDistinctPropertyValueSet(
				this.firstPFilter, coll);
		for (String f1 : filter1) {

			FilterNode fn = new FilterNode(f1);
			this.rootNodes.add(fn);

			List<String> list = this.em
					.createNamedQuery(COLLECTION_DISTINCT_VALUES_IN_FILTERED,
							String.class)
					.setParameter("pname1", this.firstPFilter)
					.setParameter("value", f1)
					.setParameter("pname2", this.secondPFilter)
					.setParameter("coll", coll).getResultList();

			for (String s : list) {
				new ElementFilterNode(fn, s);
			}

		}
	}

	public void createSecondLevel() {

	}

	private boolean isFilteringValid() {
		if (this.firstPFilter == null || this.firstPFilter.equals("")) {
			return false;
		}

		if (this.secondPFilter == null || this.secondPFilter.equals("")) {
			return false;
		}

		if (this.firstPFilter.equalsIgnoreCase(this.secondPFilter)) {
			return false;
		}

		return true;
	}

	public List<String> autocomplete(String prefix) {
		List<String> result = new ArrayList<String>();

		for (String p : knownPropertes) {
			if (p.startsWith(prefix)) {
				result.add(p);
			}
		}

		return result;
	}

	public List<TreeNode> getRootNodes() {
		return rootNodes;
	}

	public List<String> getDistinctPropertyValueSet(String pname,
			DigitalCollection collection) {
		return this.em
				.createNamedQuery(
						Constants.COLLECTION_DISTINCT_PROPERTY_VALUES_SET_QUERY)
				.setParameter("pname", pname).setParameter("coll", collection)
				.getResultList();
	}

	public String getFirstPFilter() {
		return firstPFilter;
	}

	public void setFirstPFilter(String firstPFilter) {
		this.firstPFilter = firstPFilter;
	}

	public String getSecondPFilter() {
		return secondPFilter;
	}

	public void setSecondPFilter(String secondPFilter) {
		this.secondPFilter = secondPFilter;
	}

	public NamedNode getCurrentSelection() {
		return currentSelection;
	}

	public void setCurrentSelection(NamedNode currentSelection) {
		this.currentSelection = currentSelection;
	}

}
