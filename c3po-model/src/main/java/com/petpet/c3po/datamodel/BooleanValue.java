package com.petpet.c3po.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class BooleanValue extends Value<Boolean> {

  private static final long serialVersionUID = -1005444546728731430L;

  @NotNull
  @Column(name = "bValue")
  private Boolean bValue;

  public BooleanValue() {
    this.setStatus(ValueStatus.OK.name());
  }

  public BooleanValue(Boolean v) {
    this();
    this.setValue(v.toString());
  }

  public BooleanValue(String v) {
    this();
    this.setValue(v);
  }

  @NotNull
  @Override
  public Boolean getTypedValue() {
    return this.bValue;
  }

  public void setValue(String bool) {
      this.bValue = this.getBoolValue(bool);
      super.setValue(bValue.toString());
    }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((bValue == null) ? 0 : bValue.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BooleanValue other = (BooleanValue) obj;
    if (bValue == null) {
      if (other.bValue != null) {
        return false;
      }
    } else if (!bValue.equals(other.bValue)) {
      return false;
    }
    return true;
  }

  private boolean getBoolValue(String v) {
    boolean result = false;
    if (v.equalsIgnoreCase("true")) {
      result = true;

    } else if (v.equalsIgnoreCase("false")) {
      result = false;

    } else if (v.equalsIgnoreCase("yes")) {
      result = true;

    } else if (v.equalsIgnoreCase("no")) {
      result = false;

    } else if (v.equals("1")) {
      result = true;

    } else if (v.equals("0")) {
      result = false;

    }
    
    return result; 
  }

}
