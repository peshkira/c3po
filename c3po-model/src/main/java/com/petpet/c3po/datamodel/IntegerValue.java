package com.petpet.c3po.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@NamedQueries({
    @NamedQuery(name = "getSumOfValuesForPropertyInCollection", query = "SELECT SUM(n.lValue) FROM IntegerValue n WHERE n.property.name = :pname AND n.element.collection = :coll"),
    @NamedQuery(name = "getAvgOfValuesForPropertyInCollection", query = "SELECT AVG(n.lValue) FROM IntegerValue n WHERE n.property.name = :pname AND n.element.collection = :coll"),
    @NamedQuery(name = "getMinOfValuesForPropertyInCollection", query = "SELECT MIN(n.lValue) FROM IntegerValue n WHERE n.property.name = :pname AND n.element.collection = :coll"),
    @NamedQuery(name = "getMaxOfValuesForPropertyInCollection", query = "SELECT MAX(n.lValue) FROM IntegerValue n WHERE n.property.name = :pname AND n.element.collection = :coll") })
public class IntegerValue extends Value<Long> {

  private static final long serialVersionUID = 1216578571209620108L;

  private static final Logger LOG = LoggerFactory.getLogger(IntegerValue.class);

  @NotNull
  @Column(name = "lValue")
  private Long lValue;

  public IntegerValue() {
    this.setStatus(ValueStatus.OK.name());
  }

  public IntegerValue(Long v) {
    this();
    this.setValue(v.toString());
  }

  public IntegerValue(String v) {
    this();
    this.setValue(v);
  }

  @Override
  public Long getTypedValue() {
    return this.lValue;
  }

  public void setValue(String value) {
    try {
      this.lValue = Long.valueOf(value);
      super.setValue(value);
    } catch (NumberFormatException nfe) {
      this.lValue = null;
      super.setValue(value);
      LOG.warn("The passed string '{}' is not a valid integer. Setting value to null.", value);
    }
  }

}
