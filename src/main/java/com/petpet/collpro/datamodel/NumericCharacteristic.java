package com.petpet.collpro.datamodel;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("NUMERIC")
public class NumericCharacteristic extends Characteristic<Long> {

    private static final long serialVersionUID = 5810711735788927861L;

    public NumericCharacteristic() {
        this.setType(Type.NUMERIC);
    }
    
    public NumericCharacteristic(String name, Long value) {
        this();
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    @Basic
    @Column(name="VALUE", nullable=false)
    public Long getValue() {
        return this.value;
    }

}
