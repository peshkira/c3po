package com.petpet.collpro.datamodel;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("CHAR")
public class CharCharacteristic extends Characteristic<Characteristic<?>> {

    private static final long serialVersionUID = 6871405719212787066L;
    
    public CharCharacteristic() {
        this.setType(Type.CHARACTERISTIC);
    }
    
    public CharCharacteristic(String name, Characteristic<?> value) {
        this();
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public void setValue(Characteristic<?> value) {
        this.value = value;
    }

    @Override
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    public Characteristic<?> getValue() {
        return this.value;
    }

}
