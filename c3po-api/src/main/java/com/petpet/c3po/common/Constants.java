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

  public static final String TBL_ELEMENTS = "elements";

  public static final String TBL_PROEPRTIES = "properties";

  public static final String TBL_SOURCES = "sources";

  public static final String CNF_COLLECTION_NAME = "c3po.collection.name";

  public static final String CNF_COLLECTION_LOCATION = "c3po.collection.location";

  public static final String CNF_INFER_DATE = "adaptor.inference.date";

  public static final String CNF_COLLECTION_ID = "adaptor.collection.identifier";

  public static final String CNF_RECURSIVE = "c3po.recursive";

  public static final String CNF_THREAD_COUNT = "c3po.thread.count";

  public static final String CNF_DB_HOST = "db.host";

  public static final String CBF_DB_PORT = "db.port";

  public static final String CNF_DB_NAME = "db.name";

  public static final String HISTOGRAM_MAP = "function map() {if (this.metadata['{}'].status !== 'CONFLICT') {emit(this.metadata['{}'].value, 1);}}";

  public static final String HISTOGRAM_REDUCE = "function reduce(key, values) {var res = 0;values.forEach(function (v) {res += v;});return res;}";
  
  public static final String AGGREGATE_MAP = "function map() {emit(1,{sum: this.metadata['{1}'].value, min: this.metadata['{1}'].value,max: this.metadata['{1}'].value,count:1,diff: 0,});}";

  public static final String FILTER_AGGREGATE_MAP = "function map() {if (this.metadata['{1}'].value === '{2}') {emit(1,{sum: this.metadata['{3}'].value, min: this.metadata['{3}'].value,max: this.metadata['{3}'].value,count:1,diff: 0,});}}";

  public static final String AGGREGATE_REDUCE = "function reduce(key, values) {var a = values[0];for (var i=1; i < values.length; i++){var b = values[i];var delta = a.sum/a.count - b.sum/b.count;var weight = (a.count * b.count)/(a.count + b.count);a.diff += b.diff + delta*delta*weight;a.sum += b.sum;a.count += b.count;a.min = Math.min(a.min, b.min);a.max = Math.max(a.max, b.max);}return a;}";

  public static final String AGGREGATE_FINALIZE = "function finalize(key, value){ value.avg = value.sum / value.count;value.variance = value.diff / value.count;value.stddev = Math.sqrt(value.variance);return value;}";

  // "function reduce(key, values) {var res = {count: 0}; values.forEach(function (v) {res.count += v.count}); return res;}";
  
  private Constants() {

  }
}
