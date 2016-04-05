/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.petpet.c3po.api.model.helper;

import java.io.Serializable;

/**
 * Encapsulates a single filter condition of a filter. Example: Field is the
 * mimetype property and value is a string "application/pdf", then this
 * condition means that mimetype has to be application/pdf. If the value is
 * null, then the condition should be interpreted as exists (e.g. any value for
 * the given field).
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class FilterCondition implements Serializable {

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

  public void setProperty( String f ) {
    this.field = f;
  }

  public Object getValue() {
    return value;
  }

  public void setValue( Object value ) {
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
  public boolean equals( Object obj ) {
    if ( this == obj )
      return true;
    if ( obj == null )
      return false;
    if ( getClass() != obj.getClass() )
      return false;
    FilterCondition other = (FilterCondition) obj;
    if ( field == null ) {
      if ( other.field != null )
        return false;
    } else if ( !field.equals( other.field ) )
      return false;
    if ( value == null ) {
      if ( other.value != null )
        return false;
    } else if ( !value.equals( other.value ) )
      return false;
    return true;
  }

}
