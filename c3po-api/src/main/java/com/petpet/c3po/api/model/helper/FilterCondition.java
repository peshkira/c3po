package com.petpet.c3po.api.model.helper;


/**
 * Encapsulates a single filter condition of a filter. Example: Field is the
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
  private String field;

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
   * @param f
   *          the field
   * @param v
   *          the value for the property condition.
   */
  public FilterCondition(String f, Object v) {
    field = f;
    value = v;
  }

  public String getField() {
    return field;
  }

  public void setProperty(String f) {
    this.field = f;
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
    result = prime * result + ((field == null) ? 0 : field.hashCode());
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
    if (field == null) {
      if (other.field != null)
        return false;
    } else if (!field.equals(other.field))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

}
