package com.petpet.c3po.datamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * A single metadata record of an element.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MetadataRecord {

  /**
   * The status of the element shows the certainty with which the value for the
   * given property is correct.
   * 
   * @author Petar Petrov <me@petarpetrov.org>
   * 
   */
  public static enum Status {
    /**
     * Means that more than one tools confirm the value.
     */
    OK,

    /**
     * Only one tool has given this value.
     */
    SINGLE_RESULT,

    /**
     * Signals that the tool was not confident in the extracted value.
     */
    PARTIAL,

    /**
     * One, two or more tools have provided different values for the same
     * property.
     */
    CONFLICT
  }

  /**
   * The property to which the value of this record belongs.
   */
  private Property property;

  /**
   * The actual measured value.
   */
  private String value;

  /**
   * The status of the value.
   * 
   * @see Status
   */
  private String status;

  /**
   * A list of sources that have measured the value.
   */
  private List<String> sources;

  /**
   * Creates an empty record with a status ok.
   */
  public MetadataRecord() {
    this.sources = new ArrayList<String>();
    this.status = Status.OK.name();
  }

  /**
   * Creates an record for the given property with the given value and a status
   * SINGLE_RESULT.
   * 
   * @param p
   * @param value
   */
  public MetadataRecord(Property p, String value) {
    this();
    this.property = p;
    this.value = value;
    this.status = Status.SINGLE_RESULT.name();
  }

  /**
   * Creates a record for the given property with the given value and the given
   * status.
   * 
   * @param p
   * @param value
   * @param status
   */
  public MetadataRecord(Property p, String value, Status status) {
    this(p, value);
    this.status = status.name();
  }

  public Property getProperty() {
    return this.property;
  }

  public void setProperty(Property p) {
    this.property = p;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<String> getSources() {
    return sources;
  }

  public void setSources(List<String> sources) {
    this.sources = sources;
  }
}
