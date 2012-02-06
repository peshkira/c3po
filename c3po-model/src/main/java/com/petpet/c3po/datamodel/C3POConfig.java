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
    DEFAULT, FS, SSH, RODA, ESD, ROSETTA
  }
  
  public static final String NAME = "config.name";
  
  public static final String DESCRIPTION = "config.description";
  
  public static final String LOCATION = "config.location";
  
  public static final String USER_NAME = "config.username";
  
  public static final String PASSWORD = "config.password";

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
  
}
