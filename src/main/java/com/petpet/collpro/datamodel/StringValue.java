package com.petpet.collpro.datamodel;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class StringValue extends Value<String> {

    @NotNull
    private String sValue;
    
    public StringValue() {
        
    }
    
    public StringValue(String v) {
        this.sValue = v;
    }
    
    @Override
    public String getValue() {
        return this.sValue;
    }

    @Override
    public void setValue(String value) {
        this.sValue = value;
    }
    
}
