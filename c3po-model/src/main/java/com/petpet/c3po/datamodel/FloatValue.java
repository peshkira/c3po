package com.petpet.c3po.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class FloatValue extends Value<Double> {

  private static final long serialVersionUID = -2966772666922044714L;

  private static final Logger LOG = LoggerFactory.getLogger(IntegerValue.class);

  @NotNull
  @Column(name = "fValue")
  private Double fValue;

  public FloatValue() {
    this.setStatus(ValueStatus.OK.name());
  }

  public FloatValue(Double v) {
    this();
    this.setValue(v.toString());
  }

  public FloatValue(String v) {
    this();
    this.setValue(v);
  }

  public void setValue(String value) {
    try {
      this.fValue = Double.valueOf(value);
      super.setValue(value);
    } catch (NumberFormatException nfe) {
      this.fValue = null;
      super.setValue(null);
      LOG.warn("The passed string '{}' is not a number. Setting value to null.", value);
    }
  }

  @Override
  public Double getTypedValue() {
    return this.fValue;
  }

}
