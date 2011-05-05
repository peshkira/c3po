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
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;


@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@MappedSuperclass
public abstract class Characteristic<T> implements Serializable {

    private static final long serialVersionUID = 6769620816517625541L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_CHAR")
    @SequenceGenerator(name="SEQ_CHAR", sequenceName = "characteristic_sequence")
    private long id;

    protected T value;
    
    @Basic
    @Column(name = "NAME", nullable = false)
    protected String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    protected Type type;
    
    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

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

    public void setValue(T value) {
        this.value = value;
    }

    public abstract T getValue();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Characteristic<?> other = (Characteristic<?>) obj;
        if (id != other.id)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type != other.type)
            return false;
        return true;
    }
    
}
