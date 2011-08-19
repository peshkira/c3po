package com.petpet.collpro.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@NamedQueries( {
    @NamedQuery(name = "getSumOfValuesForProperty", query = "SELECT SUM(n.lValue) FROM NumericValue n WHERE n.property.name = :pname"),
    @NamedQuery(name = "getAvgOfValuesForProperty", query = "SELECT AVG(n.lValue) FROM NumericValue n WHERE n.property.name = :pname")})
public class NumericValue extends Value<Long> {
    
    private static final long serialVersionUID = 1216578571209620108L;
    
    private static final Logger LOG = LoggerFactory.getLogger(NumericValue.class);
    
    @NotNull
    @Column(name = "lValue")
    private Long lValue;
    
    public NumericValue() {
        this.setStatus(ValueStatus.OK);
    }
    
    public NumericValue(Long v) {
        this();
        this.setTypedValue(v);
    }
    
    public NumericValue(String v) {
        this();
        
        try {
            this.setTypedValue(Long.valueOf(v));
        } catch (NumberFormatException nfe) {
            this.lValue = null;
            this.setValue(null);
            LOG.warn("The passed string '{}' is not a number. Setting value to null.", v);
        }
    }
    
    @Override
    public Long getTypedValue() {
        return this.lValue;
    }
    
    @Override
    public void setTypedValue(Long value) {
        this.lValue = value;
        this.setValue(value.toString());    
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
