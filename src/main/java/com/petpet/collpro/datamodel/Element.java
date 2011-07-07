package com.petpet.collpro.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

@Entity
public class Element {
    
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    
    @NotNull
    private String name;
    
    @NotNull
    private String path;

    @OneToMany
    private Set<Element> elements;
    
    @OneToMany(mappedBy = "element")
    private Set<Property> properties;
    
    public Element() {
        super();
        this.elements = new HashSet<Element>();
        this.properties = new HashSet<Property>();
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

    public void setProperties(Set<Property> properties) {
        this.properties = properties;
    }

    public Set<Property> getProperties() {
        return properties;
    } 
}
