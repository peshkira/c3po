package com.petpet.c3po.api.model.helper;

import com.petpet.c3po.api.model.Property;

/**
 * Encapsulates a single filter condition of a filter. Example: Property is the
 * mimetype property and value is a string "application/pdf", then this
 * condition means that mimetype has to be application/pdf.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class FilterCondition {

  /**
   * The property of this condition.
   */
  private Property property;

  /**
   * The value of this condition.
   */
  private Object value;

  /**
   * Default empty constructor.
   */
  public FilterCondition() {

  }

  /**
   * Sets the fields of this object.
   * 
   * @param p
   *          the property
   * @param v
   *          the value for the property condition.
   */
  public FilterCondition(Property p, Object v) {
    property = p;
    value = v;
  }

  public Property getProperty() {
    return property;
  }

  public void setProperty(Property property) {
    this.property = property;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((property == null) ? 0 : property.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    FilterCondition other = (FilterCondition) obj;
    if (property == null) {
      if (other.property != null)
        return false;
    } else if (!property.equals(other.property))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

}
