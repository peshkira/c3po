package com.petpet.collpro.datamodel;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class NumericValue extends Value<Long> {
    
    private static final long serialVersionUID = 1216578571209620108L;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((lValue == null) ? 0 : lValue.hashCode());
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
        NumericValue other = (NumericValue) obj;
        if (lValue == null) {
            if (other.lValue != null) {
                return false;
            }
        } else if (!lValue.equals(other.lValue)) {
            return false;
        }
        return true;
    }
    
}
