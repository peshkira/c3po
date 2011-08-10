package com.petpet.collpro.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class BooleanValue extends Value<Boolean> {

    private static final long serialVersionUID = -1005444546728731430L;

    @NotNull
    @Column(name = "bValue")
    private Boolean value;

    public BooleanValue() {
        
    }
    
    public BooleanValue(Boolean v) {
        this.value = v;
    }
    
    public BooleanValue(String v) {
        this(Boolean.valueOf(v));
    }
    
    @NotNull
    
    @Override
    public Boolean getValue() {
        return this.value;
    }
    
    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }
    
}
