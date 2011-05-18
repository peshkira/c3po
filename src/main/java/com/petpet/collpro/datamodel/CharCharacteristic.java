package com.petpet.collpro.datamodel;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.petpet.collpro.db.DBManager;

@Entity
@DiscriminatorValue("CHAR")
public class CharCharacteristic extends Characteristic {

    private static final long serialVersionUID = 6871405719212787066L;
    
    @Basic
    @Column(name = "CHAR_ID", nullable=false)
    private long characteristicId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", insertable = false, updatable = false, nullable = false)
    private Type characteristicType;
    
    public CharCharacteristic() {
        this.setType(Type.CHARACTERISTIC);
    }
    
    public CharCharacteristic(String name, Long charId, Type characteristicType) {
        this();
        this.setName(name);
        this.setCharacteristicId(charId);
        this.setCharacteristicType(characteristicType);
    }

    public void setCharacteristicId(Long charId) {
        this.characteristicId = charId;
    }

    public long getCharacteristicId() {
        return this.characteristicId;
    }
    
    public void setCharacteristicType(Type characteristicType) {
        this.characteristicType = characteristicType;
    }

    public Type getCharacteristicType() {
        return characteristicType;
    }

    @Override
    public Characteristic getValue() {
        Characteristic result = null;
        EntityManager em = DBManager.getInstance().getEntityManager();
        switch (characteristicType) {
            case BOOLEAN: result = em.find(BoolCharacteristic.class, characteristicId); break;
            case NUMERIC: result = em.find(NumericCharacteristic.class, characteristicId); break;
            case CHARACTERISTIC: result = em.find(CharCharacteristic.class, characteristicId); break;
            case STRING: result = em.find(StringCharacteristic.class, characteristicId); break;
        }
        
        return result;
    }

}
