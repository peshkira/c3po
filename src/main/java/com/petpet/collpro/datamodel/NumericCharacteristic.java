package com.petpet.collpro.datamodel;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("NUMERIC")
public class NumericCharacteristic extends Characteristic {

    private static final long serialVersionUID = 5810711735788927861L;

    @Basic
    @Column(name="VALUE", nullable=false)
    private Long value;
    
    public NumericCharacteristic() {
        this.setType(Type.NUMERIC);
    }
    
    public NumericCharacteristic(String name, Long value) {
        this();
        this.setName(name);
        this.setValue(value);
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Long getValue() {
        return this.value;
    }

}
