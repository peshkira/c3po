package com.petpet.c3po.datamodel;

import javax.persistence.Entity;

@Entity
public class StringValue extends Value<String> {

    private static final long serialVersionUID = 3382886583103355484L;

    public StringValue() {
        this.setStatus(ValueStatus.OK);
    }
    
    public StringValue(String v) {
        this();
        this.setValue(v);
    }
    
    @Override
    public String getTypedValue() {
        return this.getValue();
    }

    @Override
    public void setTypedValue(String value) {
        this.setValue(value);
    }
}
