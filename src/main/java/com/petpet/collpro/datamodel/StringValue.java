package com.petpet.collpro.datamodel;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

@Entity
@NamedQuery(name="getMD5ChecksumValue", query="SELECT v FROM StringValue v WHERE v.property.name LIKE 'md5checksum' AND v.sValue LIKE :hash")
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
