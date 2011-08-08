package com.petpet.collpro.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@NamedQuery(name="ALL_PROPERTIES", query="SELECT p FROM Property p")
public class Property {
    
    @Id @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;
    
    private String name;
    
    @Enumerated(EnumType.STRING)
    private PropertyType type;
    
    @OneToMany
    private Set<Property> properties;
    
    public Property() {
        super();
        this.properties = new HashSet<Property>();
        this.type = PropertyType.DEFAULT;
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

    public void setProperties(Set<Property> properties) {
        this.properties = properties;
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    public PropertyType getType() {
        return type;
    }
}
