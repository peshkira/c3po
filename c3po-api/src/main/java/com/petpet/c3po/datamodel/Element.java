package com.petpet.c3po.datamodel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.petpet.c3po.datamodel.Property.PropertyType;

public class Element {

  private static final Logger LOG = LoggerFactory.getLogger(Element.class);

  private static final String[] PATTERNS = { "yyyy:MM:dd HH:mm:ss", "MM/dd/yyyy HH:mm:ss", "dd MMM yyyy HH:mm",
      "EEE dd MMM yyyy HH:mm", "EEE, MMM dd, yyyy hh:mm:ss a", "EEE, MMM dd, yyyy hh:mm a", "EEE dd MMM yyyy HH.mm",
      "HH:mm MM/dd/yyyy", "yyyyMMddHHmmss" };

  private String collection;

  private String name;

  private String uid;

  private List<MetadataRecord> metadata;

  public Element(String uid, String name) {
    this.uid = uid;
    this.name = name;
  }

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

  /*
   * experimental only for the SB archive
   */
  public void extractCreatedMetadataRecord(Property created) {
    String[] split = name.split("-");
    if (split.length > 2) {
      String date = split[2];
      // System.out.println(name);
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

  private Double getDoubleValue(String value) {
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      LOG.warn("Value {} is not an float", value);
      return null;
    }
  }

  private Integer getIntegerValue(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      LOG.warn("Value {} is not an integer", value);
      return null;
    }
  }

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

  private Date parseDate(DateFormat fmt, String d) {
    try {
      return fmt.parse(d);
    } catch (ParseException e) {
      LOG.trace("date could not be parsed: {}", e.getMessage());
      return null;
    }
  }

  public BasicDBObject getDocument() {
    final BasicDBObject element = new BasicDBObject();
    element.put("name", this.getName());
    element.put("uid", this.getUid());
    element.put("collection", this.getCollection());

    final List<BasicDBObject> meta = new ArrayList<BasicDBObject>();
    for (MetadataRecord r : this.getMetadata()) {
      final BasicDBObject md = new BasicDBObject();
      md.put("key", r.getProperty().getId());
      md.put("value", this.getTypedValue(r.getProperty().getType(), r.getValue()));
      md.put("status", r.getStatus());
      md.put("sources", r.getSources());
      meta.add(md);
    }

    element.put("metadata", meta);

    return element;
  }

}
