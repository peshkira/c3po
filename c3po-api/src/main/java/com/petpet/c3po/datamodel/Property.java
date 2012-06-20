package com.petpet.c3po.datamodel;

import com.mongodb.BasicDBObject;

/**
 * A domain object encapsulating a property document.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class Property {

  /**
   * An enumeration of the property types.
   * 
   * @author Petar Petrov <me@petarpetrov.org>
   * 
   */
  public enum PropertyType {
    STRING, BOOL, INTEGER, FLOAT, DATE, ARRAY
  }

  /**
   * The id of the proeprty.
   */
  private String id;

  /**
   * The key of the property.
   */
  private String key;

  /*
   * currently not used
   */
  /**
   * Some human readable name of the property.
   */
  private String name;

  /*
   * currently not used
   */
  /**
   * A description of the property.
   */
  private String description;

  /**
   * The type of the property.
   */
  private String type;

  /**
   * A default constructor.
   */
  public Property() {

  }

  /**
   * Creates a new property with the given key and name. It genrates a ranomd
   * uuid as an id and sets the type as String.
   * 
   * @param key
   *          the key of the property
   * @param name
   *          the name of the property.
   */
  public Property(String key, String name) {
    this.id = key;
    this.setKey(key);
    this.setName(name);
    this.setType(PropertyType.STRING.name());
  }

  /**
   * Creates a new property with the given key and name. It genrates a ranomd
   * uuid as an id and sets the type as the given type.
   * 
   * @param key
   *          the key of the property
   * @param name
   *          the name of the property
   * @param type
   *          the type of the property.
   */
  public Property(String key, String name, PropertyType type) {
    this(key, name);
    this.setType(type.name());
  }

  /**
   * Creates a new property with the given key and name. It genrates a ranomd
   * uuid as an id and sets the type as the given type and the given
   * description.
   * 
   * @param key
   *          the key of the property
   * @param name
   *          the name of the property
   * @param type
   *          the type of the property.
   * @param desc
   *          the description of the property
   */
  public Property(String key, String name, PropertyType type, String desc) {
    this(key, name, type);
    this.setDescription(desc);
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  /**
   * Obtains the BSON Object representing the document of this property.in the
   * document store. Currently only the id, key and type are added.
   * 
   * @return the document of the property.
   */
  public BasicDBObject getDocument() {
    final BasicDBObject property = new BasicDBObject();
    property.put("_id", this.getId());
    property.put("key", this.getKey());
    property.put("type", this.getType());

    return property;
  }

}
