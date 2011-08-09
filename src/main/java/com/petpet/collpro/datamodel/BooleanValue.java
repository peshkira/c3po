package com.petpet.collpro.datamodel;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class BooleanValue extends Value<Boolean> {

    private static final long serialVersionUID = -1005444546728731430L;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((bValue == null) ? 0 : bValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BooleanValue other = (BooleanValue) obj;
        if (bValue == null) {
            if (other.bValue != null) {
                return false;
            }
        } else if (!bValue.equals(other.bValue)) {
            return false;
        }
        return true;
    }
    
}
