package com.petpet.c3po.datamodel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class ValueSource {
    
    @Id @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;
    
    @NotNull
    private String name;
    
    private String version;
    
    private int reliability;

    public ValueSource() {
        super();
    }
    
    public ValueSource(String name) {
        this();
        this.name = name;
    }
    
    public ValueSource(String name, String version) {
        this(name);
        this.version = version;
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
