package com.petpet.collpro.datamodel;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class NumericValue extends Value<Long> {
    
    @NotNull
    private Long lValue;
    
    @Override
    public Long getValue() {
        return this.lValue;
    }
    
    @Override
    public void setValue(Long value) {
        this.lValue = value;
    }
    
}
