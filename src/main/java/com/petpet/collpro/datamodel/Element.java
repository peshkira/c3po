package com.petpet.collpro.datamodel;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Element {

    @Id
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    
}
