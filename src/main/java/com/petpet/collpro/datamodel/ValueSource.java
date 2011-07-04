package com.petpet.collpro.datamodel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ValueSource {
    
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    private String name;
    
    private String version;
    
    private int reliability;

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

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setReliability(int reliability) {
        this.reliability = reliability;
    }

    public int getReliability() {
        return reliability;
    }
}
