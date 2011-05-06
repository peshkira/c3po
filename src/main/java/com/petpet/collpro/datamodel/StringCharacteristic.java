package com.petpet.collpro.datamodel;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("STRING")
public class StringCharacteristic extends Characteristic {

    private static final long serialVersionUID = -4254678614945423398L;
    
    @Basic
    @Column(name = "VALUE", nullable=false)
    private String value;
    
    public StringCharacteristic() {
        this.setType(Type.STRING);
    }
    
    public StringCharacteristic(String name, String value) {
        this();
        this.setName(name);
        this.setValue(value);
    }
    
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
