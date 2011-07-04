package com.petpet.collpro.datamodel;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Property {
    
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    private String name;
    
    @ManyToOne
    private Element element;

    @OneToMany
    private Set<Property> properties;
    
    @OneToMany(mappedBy = "property")
    private Set<Value<?>> values;

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

    public void setProperties(Set<Property> properties) {
        this.properties = properties;
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public void setValues(Set<Value<?>> values) {
        this.values = values;
    }

    public Set<Value<?>> getValues() {
        return values;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}
