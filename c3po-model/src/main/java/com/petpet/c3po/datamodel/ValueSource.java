package com.petpet.c3po.datamodel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@NamedQuery(name="getValueSourceByNameAndVersion", query = "SELECT vs FROM ValueSource vs WHERE vs.name = :name AND vs.version = :version")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "version"})})
public class ValueSource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @NotNull
  private String name;

  @NotNull
  private String version;

  private int reliability;

  public ValueSource() {
    super();
  }

  public ValueSource(String name) {
    this();
    this.name = name;
  }

  public ValueSource(String name, String version) {
    this(name);
    this.version = version;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getVersion() {
    return version;
  }

  public void setReliability(int reliability) {
    this.reliability = reliability;
  }

  public int getReliability() {
    return reliability;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + reliability;
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
    ValueSource other = (ValueSource) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (reliability != other.reliability)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return this.getName() + " " + this.getVersion();
  }
}
