package com.petpet.collpro.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Element {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    
    @Basic
    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Basic
    @Column(name = "FILEPATH", nullable = false)
    private String filepath;
    
    @ManyToOne
    private Collection collection;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<Characteristic<?>> info;
    
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<Characteristic<?>> identification;
    
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<Characteristic<?>> metadata;
    
    public Element() {
        this.setInfo(new HashSet<Characteristic<?>>());
        this.setIdentification(new HashSet<Characteristic<?>>());
        this.setMetadata(new HashSet<Characteristic<?>>());
    }
    
    public Element(String name, String filepath) {
        this();
        this.setName(name);
        this.setFilepath(filepath);
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

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setInfo(Set<Characteristic<?>> info) {
        this.info = info;
    }

    public Set<Characteristic<?>> getInfo() {
        return info;
    }

    public void setIdentification(Set<Characteristic<?>> identification) {
        this.identification = identification;
    }

    public Set<Characteristic<?>> getIdentification() {
        return identification;
    }

    public void setMetadata(Set<Characteristic<?>> metadata) {
        this.metadata = metadata;
    }

    public Set<Characteristic<?>> getMetadata() {
        return metadata;
    }
    
    
}
