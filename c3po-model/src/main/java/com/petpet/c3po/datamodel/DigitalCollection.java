package com.petpet.c3po.datamodel;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

@Entity
@NamedQueries({
	@NamedQuery(name = "getAllCollections", query = "SELECT c FROM DigitalCollection c"),
	@NamedQuery(name = "getAllCollectionNames", query = "SELECT c.name FROM DigitalCollection c"),
	@NamedQuery(name = "getCollectionByName", query="SELECT c FROM DigitalCollection c WHERE c.name = :name")
})

public class DigitalCollection implements Serializable {

	private static final long serialVersionUID = 7193787656462513744L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull
	private String name;

	@OneToMany(mappedBy = "collection", cascade = CascadeType.REMOVE)
	private Set<Element> elements;

	public DigitalCollection() {
		super();
		this.setElements(new HashSet<Element>());
	}

	public DigitalCollection(String name) {
		this();
		this.setName(name);
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

	public void setElements(Set<Element> elements) {
		this.elements = elements;
	}

	public Set<Element> getElements() {
		return elements;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		DigitalCollection other = (DigitalCollection) obj;
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
		return true;
	}

}
