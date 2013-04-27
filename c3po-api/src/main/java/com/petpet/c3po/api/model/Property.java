package com.petpet.c3po.api.model;

import com.mongodb.BasicDBObject;
import com.petpet.c3po.api.model.helper.PropertyType;

/**
 * A domain object encapsulating a property document.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class Property implements Model {

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
  @Deprecated
  private String name;

  /*
   * currently not used
   */
  /**
   * A description of the property.
   */
  @Deprecated
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
  @Deprecated
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
  @Deprecated
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
  @Deprecated
  public Property(String key, String name, PropertyType type, String desc) {
    this(key, name, type);
    this.setDescription(desc);
  }

  /**
   * Creates a property with the given key as key and id and sets the type to a
   * string.
   * 
   * @param key
   *          the key of the property.
   */
  public Property(String key) {
    this.id = key;
    this.key = key;
    this.type = PropertyType.STRING.name();
  }

  /**
   * Creates a property with the given key as key and id and sets the type to
   * the given type.
   * 
   * @param key
   * @param type
   */
  public Property(String key, PropertyType type) {
    this(key);
    this.type = type.name();
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

  @Deprecated
  public String getName() {
    return name;
  }

  @Deprecated
  public void setName(String name) {
    this.name = name;
  }

  @Deprecated
  public String getDescription() {
    return description;
  }

  @Deprecated
  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((key == null) ? 0 : key.hashCode());
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
    Property other = (Property) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (key == null) {
      if (other.key != null)
        return false;
    } else if (!key.equals(other.key))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

  /**
   * Obtains the BSON Object representing the document of this property.in the
   * document store. Currently only the id, key and type are added.
   * 
   * @return the document of the property.
   */
  @Deprecated
  public BasicDBObject getDocument() {
    final BasicDBObject property = new BasicDBObject();
    property.put("_id", this.getId());
    property.put("key", this.getKey());
    property.put("type", this.getType());

    return property;
  }

}
