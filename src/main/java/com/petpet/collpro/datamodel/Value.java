package com.petpet.collpro.datamodel;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@NamedQuery(name="getValueByName", query="SELECT v FROM Value v WHERE v.property.name LIKE :name")
public abstract class Value<T> implements Serializable {
    
    private static final long serialVersionUID = -896459317140318025L;

    @Id @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;

    @NotNull
    private long measuredAt;
    
    @Min(0)
    @Max(100)
    private int reliability;

    @ManyToOne
    private Property property;
    
    @ManyToOne
    private ValueSource source;
    
    @ManyToOne
    private Element element;
    
    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setMeasuredAt(long measuredAt) {
        this.measuredAt = measuredAt;
    }

    public long getMeasuredAt() {
        return measuredAt;
    }

    public void setReliability(int reliability) {
        this.reliability = reliability;
    }

    public int getReliability() {
        return reliability;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Property getProperty() {
        return property;
    }

    public void setSource(ValueSource source) {
        this.source = source;
    }

    public ValueSource getSource() {
        return source;
    }

    public abstract void setValue(T value);

    public abstract T getValue();

    public void setElement(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + (int) (measuredAt ^ (measuredAt >>> 32));
        result = prime * result + ((property == null) ? 0 : property.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Value other = (Value) obj;
        if (element == null) {
            if (other.element != null) {
                return false;
            }
        } else if (!element.equals(other.element)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (measuredAt != other.measuredAt) {
            return false;
        }
        if (property == null) {
            if (other.property != null) {
                return false;
            }
        } else if (!property.equals(other.property)) {
            return false;
        }
        if (source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!source.equals(other.source)) {
            return false;
        }
        return true;
    }
    
    
}
