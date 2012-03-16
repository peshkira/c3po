package com.petpet.c3po.datamodel;

import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.sun.istack.NotNull;

@Entity
public class C3POConfig {

  public enum GathererType {
    FS, SSH, RODA, ESD, ROSETTA
  }

  public static final String NAME = "config.name";

  public static final String DESCRIPTION = "config.description";

  public static final String LOCATION = "config.location";

  public static final String USER_NAME = "config.username";

  public static final String PASSWORD = "config.password";

  public static final String RECURSIVE = "config.recursive";

  @Id
  @GeneratedValue
  @NotNull
  private long id;

  @ElementCollection
  private Map<String, String> configs;

  @Enumerated(EnumType.ORDINAL)
  private GathererType type;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public GathererType getType() {
    return type;
  }

  public void setType(GathererType type) {
    this.type = type;
  }

  public Map<String, String> getConfigs() {
    return configs;
  }

  public void setConfigs(Map<String, String> configs) {
    this.configs = configs;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((configs == null) ? 0 : configs.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    C3POConfig other = (C3POConfig) obj;
    if (configs == null) {
      if (other.configs != null)
        return false;
    } else if (!configs.equals(other.configs))
      return false;
    if (type != other.type)
      return false;
    return true;
  }

}
