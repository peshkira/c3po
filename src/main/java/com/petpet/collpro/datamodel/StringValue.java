package com.petpet.collpro.datamodel;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

@Entity
@NamedQuery(name="getMD5ChecksumValue", query="SELECT v FROM StringValue v WHERE v.property.name LIKE 'md5checksum' AND v.sValue LIKE :hash")
public class StringValue extends Value<String> {

    private static final long serialVersionUID = 3382886583103355484L;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sValue == null) ? 0 : sValue.hashCode());
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
        StringValue other = (StringValue) obj;
        if (sValue == null) {
            if (other.sValue != null) {
                return false;
            }
        } else if (!sValue.equals(other.sValue)) {
            return false;
        }
        return true;
    }
    
}
