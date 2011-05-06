package com.petpet.collpro.datamodel;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("BOOL")
public class BoolCharacteristic extends Characteristic {

    private static final long serialVersionUID = 172240703263205522L;
    
    @Basic
    @Column(name = "VALUE", nullable=false)
    private boolean value;
    
    public BoolCharacteristic() {
        this.setType(Type.BOOLEAN);
    }
    
    public BoolCharacteristic(String name, Boolean value) {
        this();
        this.setName(name);
        this.setValue(value);
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    public Boolean getValue() {
        return this.value;
    }

}
