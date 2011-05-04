package com.petpet.collpro.datamodel;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;


@Entity(name="CHARACTERISTIC")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class Characteristic<T> implements Serializable {

    private static final long serialVersionUID = 6769620816517625541L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_CHAR")
    @SequenceGenerator(name="SEQ_CHAR", sequenceName = "SEQ_CHAR")
    @Column(name = "ID", updatable = false, insertable = false, nullable = false)
    @SuppressWarnings("unused")
    private long id;
    
    @Basic
    @Column(name = "NAME", nullable=false)
    protected String name;
    
    @Enumerated(EnumType.STRING)
    protected Type type;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public abstract void setValue(T value);

    public abstract T getValue();
    
    
}
