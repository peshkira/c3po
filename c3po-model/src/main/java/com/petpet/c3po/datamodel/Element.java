package com.petpet.c3po.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "element", uniqueConstraints = { @UniqueConstraint(columnNames = { "collection_id", "name", "uid" }) })
@org.hibernate.annotations.Table(appliesTo = "element", indexes = { @Index(name = "element_idx", columnNames = {
    "name", "uid" }) })
@NamedQueries({ @NamedQuery(name = "getElementsInCollectionCount", query = "SELECT COUNT(e) FROM Element e WHERE e.collection = :coll") })
public class Element implements Serializable {

  private static final long serialVersionUID = -7335423580873489935L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @NotNull
  private String name;

  @NotNull
  private String uid;

  @NotNull
  @OneToOne
  private DigitalCollection collection;

  // @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @Transient
  private Set<Element> elements;

  @OneToMany(mappedBy = "element", cascade = CascadeType.ALL)
  private List<Value<?>> values;

  public Element() {
    super();
    this.elements = new HashSet<Element>();
    this.values = new ArrayList<Value<?>>();
  }

  public Element(String name, String path) {
    this();
    this.name = name;
    this.uid = path;
  }

  public Element(String name, String path, DigitalCollection collection) {
    this(name, path);
    this.collection = collection;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getUid() {
    return uid;
  }

  public void setCollection(DigitalCollection collection) {
    this.collection = collection;
  }

  public DigitalCollection getCollection() {
    return collection;
  }

  public void setElements(Set<Element> elements) {
    this.elements = elements;
  }

  public Set<Element> getElements() {
    return elements;
  }

  public void setValues(List<Value<?>> values) {
    this.values = values;
  }

  public List<Value<?>> getValues() {
    return this.values;
  }

  public void addValue(Value<?> v) {
    if (!this.values.contains(v)) {
      v.setElement(this);
      this.values.add(v);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((uid == null) ? 0 : uid.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Element other = (Element) obj;
    if (id != other.id) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (uid == null) {
      if (other.uid != null) {
        return false;
      }
    } else if (!uid.equals(other.uid)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    if (this.getName() != null) {
      return this.getName();
    }

    return super.toString();
  }

}
