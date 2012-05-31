package com.petpet.c3po.datamodel;

import java.util.UUID;

import com.mongodb.BasicDBObject;

/**
 * The source represents a tool that has extracted specific measurements of
 * elements.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class Source {

  /**
   * The id of the source.
   */
  private String id;

  /**
   * The name of the source.
   */
  private String name;

  /**
   * The version of the source.
   */
  private String version;

  /**
   * A default constructor.
   */
  public Source() {

  }

  /**
   * Creates a new source with the name and the version and auto generates an
   * id.
   * 
   * @param name
   *          the name of the source.
   * @param version
   *          the version of the source.
   */
  public Source(String name, String version) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
    this.version = version;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Gets the BSON Object representing the document of this source.
   * 
   * @return the source document.
   */
  public BasicDBObject getDocument() {
    final BasicDBObject source = new BasicDBObject();

    source.put("_id", this.getId());
    source.put("name", this.getName());
    source.put("version", this.getVersion());

    return source;
  }

}
