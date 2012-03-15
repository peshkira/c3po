package com.petpet.c3po.datamodel;

import javax.persistence.Entity;
import javax.persistence.PrePersist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class StringValue extends Value<String> {

  private static final long serialVersionUID = 3382886583103355484L;

  private static final Logger LOG = LoggerFactory.getLogger(StringValue.class);
  
  public StringValue() {
    this.setStatus(ValueStatus.OK.name());
  }

  public StringValue(String v) {
    this();
    this.setValue(v);
  }

  @Override
  public String getTypedValue() {
    return this.getValue();
  }

  @Override
  public void setTypedValue(String value) {
    this.setValue(value);
  }

  @PrePersist
  public void prepersist() {
    if (this.getValue().length() > 1000) {
      LOG.warn("String value {} is longer than 1000 characters for element {}, truncating", this.getValue(), this.getElement().getName());
      this.setValue(this.getValue().substring(0, 1000));
    }
  }
}
