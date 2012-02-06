package com.petpet.c3po.datamodel;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.sun.istack.NotNull;

@Entity
public class C3POConfig {

  private enum GathererType {
    DEFAULT, FS, SSH, RODA, ESD, ROSETTA
  }

  @Id
  @GeneratedValue
  @NotNull
  private long id;

  @Basic
  @NotNull
  private String name;

  @Basic
  private String description;

  @Basic
  @NotNull
  private String location;

  @Basic
  private String username;

  @Basic
  private String password;

  @Enumerated(EnumType.ORDINAL)
  private GathererType type;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public GathererType getType() {
    return type;
  }

  public void setType(GathererType type) {
    this.type = type;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
