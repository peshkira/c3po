package com.petpet.collpro.datamodel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class Value<T> {
    
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
}
