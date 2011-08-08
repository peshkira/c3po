package com.petpet.collpro.datamodel;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class BooleanValue extends Value<Boolean> {

    private Boolean bValue;

    public BooleanValue() {
        
    }
    
    public BooleanValue(Boolean v) {
        this.bValue = v;
    }
    
    public BooleanValue(String v) {
        this(Boolean.valueOf(v));
    }
    
    @NotNull
    
    @Override
    public Boolean getValue() {
        return this.bValue;
    }
    
    @Override
    public void setValue(Boolean value) {
        this.bValue = value;
    }
    
}
