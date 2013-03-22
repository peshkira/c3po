package com.petpet.c3po.datamodel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.MetadataRecord.Status;
import com.petpet.c3po.datamodel.Property.PropertyType;

/**
 * A domain object class that encapsulates an element document. It consists of a
 * couple of attributes that describe a simple object (usually a file) and a
 * list of specific metadata.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 */

public class Element {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Element.class);

  /**
   * Some date patterns used for date parsing.
   */
  private static final String[] PATTERNS = { "yyyy:MM:dd HH:mm:ss", "MM/dd/yyyy HH:mm:ss", "dd MMM yyyy HH:mm",
      "EEE dd MMM yyyy HH:mm", "EEE, MMM dd, yyyy hh:mm:ss a", "EEE, MMM dd, yyyy hh:mm a", "EEE dd MMM yyyy HH.mm",
      "HH:mm MM/dd/yyyy", "yyyyMMddHHmmss", "yyyy-MM-dd'T'HH:mm:ss" };
  
  private String id;

  /**
   * The collection to which the current element belongs.
   */
  private String collection;

  /**
   * Some non-unique name of this element.
   */
  private String name;

  /**
   * Some unique identifier of this element that references the original file
   * back in the source.
   */
  private String uid;

  /**
   * A list of {@link MetadataRecord} info.
   */
  private List<MetadataRecord> metadata;

  /**
   * Creates an element with the given uid and name.
   * 
   * @param uid
   * @param name
   */
  public Element(String uid, String name) {
    this.uid = uid;
    this.name = name;
  }

  /**
   * Creates an element with the given uid, name and collection.
   * 
   * @param collection
   * @param uid
   * @param name
   */
  public Element(String collection, String uid, String name) {
    this(uid, name);
    this.collection = collection;
  }

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public List<MetadataRecord> getMetadata() {
    return metadata;
  }

  public void setMetadata(List<MetadataRecord> metadata) {
    this.metadata = metadata;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * Removes all records for the given property id and returs a 
   * list of all removed metadata records.
   * 
   * @param property
   *          the id of the property
   * @return returns the records of the element matching this property that were deleted.
   */
  public List<MetadataRecord> removeMetadata(String property) {
    List<MetadataRecord> result = new ArrayList<MetadataRecord>();
    
    Iterator<MetadataRecord> iterator = this.metadata.iterator();
    while(iterator.hasNext()) {
      MetadataRecord next = iterator.next();
      if (next.getProperty().getId().equals(property)) {
        result.add(next);
        iterator.remove();
      }
    }
    
    return result;
  }

  /**
   * Tries to parse a creation date out of the name of the current element. Some
   * sources include a timestamp in the name. The usage of this method can be
   * configured through the adaptor config parameter
   * {@link Constants#CNF_INFER_DATE}
   * 
   * If the name is not set or it does not include a timestamp, the method does
   * nothing.
   * 
   * @param created
   *          the property for the creation date.
   */
  /*
   * experimental only for the SB archive
   */
  public void extractCreatedMetadataRecord(Property created) {
    if (this.name != null) {

      String[] split = name.split("-");

      if (split.length > 2) {
        String date = split[2];

        try {
          Long.valueOf(date);

          MetadataRecord c = new MetadataRecord(created, date);
          this.metadata.add(c);

        } catch (NumberFormatException nfe) {
          // if the value is not a number then it is something else and not a
          // year, skip the inference.

        }
      }
    }
  }

  /**
   * Tries to infer the type of the value based on the property type and
   * converts the value. Otherwise it leaves the string representation. This is
   * valuable as the underlying persistence layer can store the native type
   * instead of strings which makes some aggregation functions easier.
   * 
   * @param t
   *          the type of the property @see {@link PropertyType}
   * @param value
   *          the value to convert
   * @return an object with the specific type, or the original value. If the
   *         passed value was null, an empty string is returned.
   */
  public Object getTypedValue(String t, String value) {

    if (value == null) {
      return "";
    }

    PropertyType type = PropertyType.valueOf(t);
    Object result = null;
    switch (type) {
      case STRING:
        result = value;
        break;
      case BOOL:
        result = this.getBooleanValue(value);
        break;
      case INTEGER:
        result = this.getIntegerValue(value);
        break;
      case FLOAT:
        result = this.getDoubleValue(value);
        break;
      case DATE:
        result = this.getDateValue(value);
        break;
      case ARRAY:
        break;
    }

    return (result == null) ? value : result;

  }

  /**
   * Tries to convert to a date object. First the method tries to match the
   * value based on some predefined patterns. If no pattern matches the the
   * method checks if the value is a long. If nothing succeeds then null is
   * returned.
   * 
   * @param value
   *          the value to convert
   * @return a date if successful, null otherwise.
   */
  private Date getDateValue(String value) {
    LOG.trace("parsing value {} as date", value);

    final SimpleDateFormat fmt = new SimpleDateFormat();

    Date result = null;
    for (String p : PATTERNS) {

      fmt.applyPattern(p);
      result = this.parseDate(fmt, value);

      if (result != null) {
        break;
      }
    }

    if (result == null) {
      LOG.debug("No pattern matching for value {}, try to parse as long", value);
    }

    try {

      if (value.length() != 14) {
        LOG.trace("value is not 14 characters long, probably a long representation");
        result = new Date(Long.valueOf(value));
      }

    } catch (NumberFormatException e) {
      LOG.trace("date is not in long representation, trying pattern matching: {}", e.getMessage());
    }

    return result;
  }

  /**
   * Gets a double out of the passed value.
   * 
   * @param value
   *          the value to convert
   * @return null if not a floating point string.
   */
  private Double getDoubleValue(String value) {
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      LOG.warn("Value {} is not an float", value);
      return null;
    }
  }

  /**
   * Converts to integer.
   * 
   * @param value
   *          the value to convert.
   * @return the integer object or null if not a numeric value.
   */
  private Long getIntegerValue(String value) {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      LOG.warn("Value {} is not an integer", value);
      return null;
    }
  }

  /**
   * A boolean representation of the passed string. If the string equals one of
   * 'yes', 'true' or 'no', 'false' then the value is converted to the
   * corresponding boolean. Otherwise null is returned
   * 
   * @param value
   *          the value to convert
   * @return the boolean representation of the value, or null if not a boolean.
   */
  private Boolean getBooleanValue(String value) {
    if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) {
      return new Boolean(true);
    } else if (value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")) {
      return new Boolean(false);
    } else {
      LOG.warn("Value {} is not a boolean", value);
      return null;
    }
  }

  /**
   * Parses a date with the given dateformat.
   * 
   * @param fmt
   *          the dateformat object to parse the date with.
   * @param d
   *          the string to parse.
   * @return the date or null if parsing was not successful.
   */
  private Date parseDate(DateFormat fmt, String d) {
    try {
      return fmt.parse(d);
    } catch (ParseException e) {
      LOG.trace("date could not be parsed: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Gets the BSON Object representation that is stored within the mongo
   * database.
   * 
   * @return the document of this element.
   */
  public BasicDBObject getDocument() {
    final BasicDBObject element = new BasicDBObject();
    element.put("name", this.getName());
    element.put("uid", this.getUid());
    element.put("collection", this.getCollection());

    final BasicDBObject meta = new BasicDBObject();
    for (MetadataRecord r : this.getMetadata()) {
      final BasicDBObject key = new BasicDBObject();

      key.put("status", r.getStatus());

      if (r.getStatus().equals(Status.CONFLICT.name())) {
        BasicDBObject conflicting;
        List<Object> values;
        List<Object> sources;
        if (meta.containsField(r.getProperty().getId())) {
          conflicting = (BasicDBObject) meta.get(r.getProperty().getId());
          values = (List<Object>) conflicting.get("values");
          sources = (List<Object>) conflicting.get("sources");
          values.add(this.getTypedValue(r.getProperty().getType(), r.getValue()));
          sources.add(r.getSources().get(0));

        } else {
          conflicting = new BasicDBObject();
          values = new ArrayList<Object>();
          sources = new ArrayList<Object>();

          values.add(this.getTypedValue(r.getProperty().getType(), r.getValue()));
          sources.add(r.getSources().get(0));
        }

        conflicting.put("values", values);
        conflicting.put("sources", sources);
        conflicting.put("status", r.getStatus());
        meta.put(r.getProperty().getId(), conflicting);

      } else {
        key.put("value", this.getTypedValue(r.getProperty().getType(), r.getValue()));
        key.put("sources", r.getSources());
        meta.put(r.getProperty().getId(), key);
      }

    }

    element.put("metadata", meta);

    return element;
  }

}
