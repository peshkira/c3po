package com.petpet.collpro.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

@Entity
@NamedQuery(name="getMD5ChecksumValue", query="SELECT v FROM StringValue v WHERE v.property.name = 'md5checksum' AND v.value = :hash")
public class StringValue extends Value<String> {

    private static final long serialVersionUID = 3382886583103355484L;

    @NotNull
    @Column(name = "sValue")
    private String value;
    
    public StringValue() {
        
    }
    
    public StringValue(String v) {
        this.value = v;
    }
    
    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public void setValue(String value) {
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
        StringValue other = (StringValue) obj;
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
