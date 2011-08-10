package com.petpet.collpro.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class NumericValue extends Value<Long> {
    
    private static final long serialVersionUID = 1216578571209620108L;

    @NotNull
    @Column(name="lValue")
    private Long value;
    
    public NumericValue() {
        
    }
    
    public NumericValue(Long v) {
        this.value = v;
    }
   
    public NumericValue(String v) {
        this(Long.valueOf(v));
    }
    
    @Override
    public Long getValue() {
        return this.value;
    }
    
    @Override
    public void setValue(Long value) {
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
        NumericValue other = (NumericValue) obj;
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
