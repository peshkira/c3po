package com.petpet.collpro.beans;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.swing.tree.TreeNode;

import org.richfaces.component.UITree;
import org.richfaces.event.TreeSelectionChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.tree.ElementFilterNode;
import com.petpet.collpro.tree.ElementNode;
import com.petpet.collpro.tree.FilterNode;

@ManagedBean
public class TreeBean {

	private static final Logger LOG = LoggerFactory.getLogger(TreeBean.class);

	@PersistenceContext
	private EntityManager em;

	private TreeNode currentSelection = null;

	private List<TreeNode> rootNodes = new ArrayList<TreeNode>();

	@PostConstruct
	public void init() {
		LOG.info("constructing tree...");
		long start = System.currentTimeMillis();
		String pname1 = "mimetype";
		String pname2 = "format";

		DigitalCollection coll = this.em.find(DigitalCollection.class, 1L);
		
		
		List<String> filter1 = this.getDistinctPropertyValueSet(pname1, coll);

		for (String f1 : filter1) {

			FilterNode fn = new FilterNode(f1);
			this.rootNodes.add(fn);

			List<String> list = this.em
					.createNamedQuery(
							"getDistinctValuesWithinPropertyFilteredCollection")
					.setParameter("pname1", pname1).setParameter("value", f1)
					.setParameter("pname2", pname2).setParameter("coll", coll)
					.getResultList();

			for (String s : list) {

				ElementFilterNode efn = new ElementFilterNode(fn, s);

				List<Element> elements = this.em
						.createQuery(
								"SELECT val.element FROM Value val WHERE val.property.name = :pname2 AND val.value = :value2 AND val.element IN (SELECT v.element FROM Value v WHERE v.property.name = :pname1 AND v.value = :value1 AND v.element.collection = :coll) ORDER BY val.element.name",
								Element.class).setParameter("pname1", pname1)
						.setParameter("value1", f1)
						.setParameter("pname2", pname2)
						.setParameter("value2", s).setParameter("coll", coll)
						.getResultList();

				for (Element e : elements) {
					new ElementNode(efn, e);
				}
			}
		}
		
		long end = System.currentTimeMillis();
		LOG.info("Tree constructed in: " + (end - start) + "ms");

	}

	public void selectionChanged(TreeSelectionChangeEvent selectionChangeEvent) {
		// considering only single selection
		List<Object> selection = new ArrayList<Object>(
				selectionChangeEvent.getNewSelection());
		Object currentSelectionKey = selection.get(0);
		UITree tree = (UITree) selectionChangeEvent.getSource();

		Object storedKey = tree.getRowKey();
		tree.setRowKey(currentSelectionKey);
		currentSelection = (TreeNode) tree.getRowData();
		tree.setRowKey(storedKey);
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
}
