package com.petpet.collpro.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Collection {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    
    @Basic
    @Column(name = "NAME", nullable = false)
    private String name;
    
    @OneToMany(mappedBy = "collection")
    private Set<Element> elements;
    
    public Collection() {
        this.elements = new HashSet<Element>();
    }

    public Collection(String name) {
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
}
