package com.petpet.c3po.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class BooleanValue extends Value<Boolean> {
    
    private static final long serialVersionUID = -1005444546728731430L;
    
    private static final Logger LOG = LoggerFactory.getLogger(BooleanValue.class);
    
    @NotNull
    @Column(name = "bValue")
    private Boolean bValue;
    
    public BooleanValue() {
        this.setStatus(ValueStatus.OK);
    }
    
    public BooleanValue(Boolean v) {
        this();
        this.setTypedValue(v);
    }
    
    public BooleanValue(String v) {
        if (v.equalsIgnoreCase("true")) {
            this.setTypedValue(true);
            
        } else if (v.equalsIgnoreCase("false")) {
            this.setTypedValue(false);
            
        } else if (v.equalsIgnoreCase("yes")) {
            this.setTypedValue(true);
            
        } else if (v.equalsIgnoreCase("no")) {
            this.setTypedValue(false);
            
        } else if (v.equals("1")) {
            this.setTypedValue(true);
            
        } else if (v.equals("0")) {
            this.setTypedValue(false);
            
        } else {
            LOG.warn("The passed string '{}' is not of boolean type, assuming false value", v);
            this.setTypedValue(false);
        }
    }
    
    @NotNull
    @Override
    public Boolean getTypedValue() {
        return this.bValue;
    }
    
    @Override
    public void setTypedValue(Boolean value) {
        this.bValue = value;
        this.setValue(value.toString());
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
