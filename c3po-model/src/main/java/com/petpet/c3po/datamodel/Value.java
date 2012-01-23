package com.petpet.c3po.datamodel;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
    @NamedQuery(name = "getAllPropertiesInCollection", query = "SELECT DISTINCT(v.property) FROM Value v WHERE v.element.collection = :coll"),
    @NamedQuery(name = "getValueByPropertyNameAndCollection", query = "SELECT v FROM Value v WHERE v.property.name = :pname AND v.element.collection = :coll"),
    @NamedQuery(name = "getValueByPropertyAndValueAndCollection", query = "SELECT v FROM Value v WHERE v.property.name = :pname AND v.value = :value AND v.element.collection = :coll"),
    @NamedQuery(name = "getElementsWithPropertyInCollectionCount", query = "SELECT COUNT(DISTINCT v.element) FROM Value v WHERE v.property.name = :pname AND v.element.collection = :coll"),
    @NamedQuery(name = "getElementsWithPropertyAndValueInCollectionCount", query = "SELECT COUNT(DISTINCT v.element) FROM Value v WHERE v.property.name = :pname AND v.value = :value AND v.element.collection = :coll"),
    @NamedQuery(name = "getElementsWithPropertyAndValueInCollectionSet", query = "SELECT v.element FROM Value v WHERE v.property.name = :pname AND v.value = :value AND v.element.collection = :coll"),
    @NamedQuery(name = "getElementsWithinDoubleFilteredCollection", query = "SELECT val.element FROM Value val WHERE val.property.name = :pname2 AND val.value = :value2 AND val.element IN (SELECT v.element FROM Value v WHERE v.property.name = :pname1 AND v.value = :value1 AND v.element.collection = :coll) ORDER BY val.element.name"),
    @NamedQuery(name = "getDistinctValuesWithinPropertyFilteredCollection", query = "SELECT DISTINCT val.value FROM Value val WHERE val.property.name = :pname2 AND val.element IN (SELECT v.element FROM Value v WHERE v.property.name = :pname1 AND v.value = :value AND v.element.collection = :coll) ORDER BY val.value"),
    @NamedQuery(name = "getDistinctPropertyValueInCollectionCount", query = "SELECT COUNT(DISTINCT v.value) FROM Value v WHERE v.property.name = :pname AND v.element.collection = :coll"),
    @NamedQuery(name = "getDistinctPropertyValuesInCollectionSet", query = "SELECT DISTINCT v.value FROM Value v WHERE v.property.name = :pname AND v.element.collection = :coll ORDER BY v.value"),
    @NamedQuery(name = "getAllValuesForElementCount", query = "SELECT COUNT(v.value) FROM Value v WHERE v.element = :element"),
    @NamedQuery(name = "getAllValuesForElement", query = "SELECT v FROM Value v WHERE v.element = :element"),
    @NamedQuery(name = "getMostOccurringPropertiesInCollection", query = "SELECT v.property.id, v.property.name, COUNT(*) AS c FROM Value v WHERE v.status != 'CONFLICT' AND v.element.collection = :coll GROUP BY v.property.id, v.property.name ORDER BY c DESC, v.property.name"),
    @NamedQuery(name = "getAllValuesInCollectionDistribution", query = "SELECT v.property.name, v.value, COUNT(*) AS c FROM Value v WHERE v.status != 'CONFLICT' AND v.element.collection = :coll GROUP BY v.value, v.property.name ORDER BY v.property.name, c DESC"),
    @NamedQuery(name = "getSpecificValueInCollectionDistribution", query = "SELECT v.property.name, v.value, COUNT(*) AS c FROM Value v WHERE v.status != 'CONFLICT' AND v.property.name=:pname AND v.element.collection = :coll GROUP BY v.value, v.property.name ORDER BY c DESC"),
    @NamedQuery(name = "getSpecificValueInDistributionInSet", query = "SELECT v.property.name, v.value, COUNT(*) AS c FROM Value v WHERE v.status != 'CONFLICT' AND v.property.name=:pname AND v.element IN (:set) GROUP BY v.value, v.property.name ORDER BY c DESC") })
public abstract class Value<T> implements Serializable {

  private static final long serialVersionUID = -896459317140318025L;

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE)
  private long id;

  @NotNull
  private long measuredAt;

  @NotNull
  @Column(length=1000)
  private String value;

  @Min(0)
  @Max(100)
  private int reliability;

  @NotNull
  @Enumerated(EnumType.STRING)
  private ValueStatus status;

  @NotNull
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  private Property property;

  @NotNull
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  private ValueSource source;

  @NotNull
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
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

  public void setStatus(ValueStatus status) {
    this.status = status;
  }

  public ValueStatus getStatus() {
    return status;
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

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public abstract void setTypedValue(T value);

  public abstract T getTypedValue();

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
