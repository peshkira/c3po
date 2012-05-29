package com.petpet.c3po.common;

public final class Constants {

  /**
   * The url for the xml schema property used by the sax parser while validating
   * xml files against their schemata.
   */
  public static final String XML_SCHEMA_PROPERTY = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

  /**
   * The url for the xml schema language used by the sax parser while validating
   * xml files against their schemata.
   */
  public static final String XML_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

  public static final String PROFILE_FORMAT_VERSION = "0.1";

  public static final String CNF_COLLECTION_NAME = "c3po.collection.name";

  public static final String CNF_COLLECTION_LOCATION = "c3po.collection.location";
  
  public static final String CNF_INFER_DATE = "c3po.inference.date";

  public static final String CNF_RECURSIVE = "c3po.recursive";

  public static final String CNF_THREAD_COUNT = "c3po.thread.count";

  public static final String CNF_DB_HOST = "db.host";

  public static final String CBF_DB_PORT = "db.port";

  public static final String CNF_DB_NAME = "db.name";

  public static final String HISTOGRAM_REDUCE = "function(key, values) {var res = {count: 0};"
      + "values.forEach(function(v) {res.count += v.count;});" + "return res;" + "}";

  public static final String HISTOGRAM_MAP = "function() {var prop = \"\";"
      + "this.metadata.forEach(function (m) {if (m.key === '{}') {prop = m.value;}});" + "emit(prop, {count:1});" + "}";

  private Constants() {

  }
}
