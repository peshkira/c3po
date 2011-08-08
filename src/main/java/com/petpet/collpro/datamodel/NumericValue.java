package com.petpet.collpro.datamodel;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class NumericValue extends Value<Long> {
    
    @NotNull
    private Long lValue;
    
    public NumericValue() {
        
    }
    
    public NumericValue(Long v) {
        this.lValue = v;
    }
   
    public NumericValue(String v) {
        this(Long.valueOf(v));
    }
    
    @Override
    public Long getValue() {
        return this.lValue;
    }
    
    @Override
    public void setValue(Long value) {
        this.lValue = value;
    }
    
}
