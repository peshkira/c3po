package com.petpet.collpro.datamodel;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

@Entity
@NamedQuery(name = "getElementsCount", query = "SELECT COUNT(e) FROM Element e")
public class Element implements Serializable {
    
    private static final long serialVersionUID = -7335423580873489935L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    @NotNull
    private String name;
    
    @NotNull
    private String path;
    
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Element> elements;
    
    @OneToMany(mappedBy = "element", cascade = CascadeType.ALL)
    private Set<Value<?>> values;
    
    public Element() {
        super();
        this.elements = new HashSet<Element>();
        this.values = new HashSet<Value<?>>();
    }
    
    public Element(String name, String path) {
        this();
        this.name = name;
        this.path = path;
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
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setElements(Set<Element> elements) {
        this.elements = elements;
    }
    
    public Set<Element> getElements() {
        return elements;
    }
    
    public void setValues(Set<Value<?>> values) {
        this.values = values;
    }
    
    public Set<Value<?>> getValues() {
        return this.values;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }
    
}
