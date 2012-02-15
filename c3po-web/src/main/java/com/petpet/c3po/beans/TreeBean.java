package com.petpet.c3po.beans;

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

import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.db.PreparedQueries;
import com.petpet.c3po.tree.ElementFilterNode;
import com.petpet.c3po.tree.ElementNode;
import com.petpet.c3po.tree.FilterNode;
import com.petpet.c3po.tree.NamedNode;

@ManagedBean
@SessionScoped
public class TreeBean implements Serializable {

  private static final long serialVersionUID = -6484069020777687927L;

  private static final Logger LOG = LoggerFactory.getLogger(TreeBean.class);

  @PersistenceContext(unitName = "C3POPersistenceUnit")
  private EntityManager em;

  private PreparedQueries queries;

  private NamedNode currentSelection = null;

  private DigitalCollection coll;

  private List<TreeNode> rootNodes;

  private List<String> knownPropertes = new ArrayList<String>();

  private String filter1;

  private String filter2;

  @PostConstruct
  public void init() {
    this.queries = new PreparedQueries(this.em);
    this.knownPropertes.addAll(this.queries.getAllPropertyNames());
    System.out.println(knownPropertes.size());
    // FIXME selection by user
    this.coll = this.em.find(DigitalCollection.class, 1L);
  }

  public void selectionChanged(TreeSelectionChangeEvent evt) {
    // LOG.debug("tree selectionChanged...");
    // considering only single selection
    List<Object> selection = new ArrayList<Object>(evt.getNewSelection());
    Object currentSelectionKey = selection.get(0);
    UITree tree = (UITree) evt.getSource();
    Object storedKey = tree.getRowKey();
    tree.setRowKey(currentSelectionKey);
    NamedNode node = (NamedNode) tree.getRowData();
    setCurrentSelection(node);
    tree.setRowKey(storedKey);
    LOG.info("current selection is '{}', name is '{}'", node.getType(), node.getName());
    
    if (node.getType().equals("elementfilter")) {
      node.getDistinctProperties();
    }
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

          List<Element> elements = this.queries.getElementsWithinDoubleFilteredCollection(this.filter1, fn.getName(),
              this.filter2, efn.getName(), this.coll);

          for (Element e : elements) {
            new ElementNode(efn, e);
          }

        }
      }
    }
  }

  public void destroyTree() {
    // TODO
  }

  public void createTree() {
    this.rootNodes = new ArrayList<TreeNode>();

    if (!this.isFilteringValid()) {
      LOG.info("filtering invalid");
      return;
    }

    LOG.info("constructing tree...");
    long start = System.currentTimeMillis();
    this.filterTree();

    long end = System.currentTimeMillis();
    LOG.info("Tree constructed in: " + (end - start) + "ms");
  }

  private void filterTree() {
    List<String> filter1 = this.getPropertyValueSet(this.filter1, coll);
    for (String f1 : filter1) {

      FilterNode fn = new FilterNode(f1);
      this.rootNodes.add(fn);

      List<String> list = this.queries.getDistinctValuesWithinFiltering(this.filter1, this.filter2, f1, coll);

      for (String s : list) {
        new ElementFilterNode(fn, s);
      }

    }
  }

  public void createSecondLevel() {

  }

  private boolean isFilteringValid() {
    if (this.filter1 == null || this.filter1.equals("")) {
      return false;
    }

    if (this.filter2 == null || this.filter2.equals("")) {
      return false;
    }

    if (this.filter1.equalsIgnoreCase(this.filter2)) {
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
    System.out.println("autocomplete");
    return result;
  }

  public List<TreeNode> getRootNodes() {
    return rootNodes;
  }

  public String getFilter1() {
    return filter1;
  }

  public void setFilter1(String firstPFilter) {
    this.filter1 = firstPFilter;
  }

  public String getFilter2() {
    return filter2;
  }

  public void setFilter2(String secondPFilter) {
    this.filter2 = secondPFilter;
  }

  public NamedNode getCurrentSelection() {
    return currentSelection;
  }

  public void setCurrentSelection(NamedNode currentSelection) {
    this.currentSelection = currentSelection;
  }

  public List<String> getPropertyValueSet(String pname, DigitalCollection coll) {
    return this.queries.getDistinctPropertyValueSet(pname, coll);

  }
}
