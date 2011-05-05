package com.petpet.collpro.datamodel;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("STRING")
public class StringCharacteristic extends Characteristic<String> {

    private static final long serialVersionUID = -4254678614945423398L;
    
    public StringCharacteristic() {
        this.setType(Type.STRING);
    }
    
    public StringCharacteristic(String name, String value) {
        this();
        this.setName(name);
        this.setValue(value);
    }
    
    @Override
    @Basic
    @Column(name = "VALUE", nullable=false)
    public String getValue() {
        return this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
