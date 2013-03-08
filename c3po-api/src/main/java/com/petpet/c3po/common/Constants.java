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

  /**
   * The version of the generated profile.
   */
  @Deprecated
  public static final String PROFILE_FORMAT_VERSION = "0.1";

  /**
   * The version of the core module.
   */
  public static final String CORE_VERSION = "0.2.0";

  public static final String API_VERSION = "0.2.0";

  /**
   * The elements collection in the document store.
   */
  public static final String TBL_ELEMENTS = "elements";

  /**
   * The properties collection in the document store.
   */
  public static final String TBL_PROEPRTIES = "properties";

  /**
   * The source collection in the document store.
   */
  public static final String TBL_SOURCES = "sources";

  /**
   * The filters stored in the db.
   */
  public static final String TBL_FILTERS = "filters";

  /**
   * The actions done on a collection basis in the db.
   */
  public static final String TBL_ACTIONLOGS = "actionlogs";

  /**
   * A c3po configuration for the collection on which to operate.
   */
  public static final String CNF_COLLECTION_NAME = "c3po.collection.name";

  /**
   * A c3po configuration for the location where the metadata is.
   */
  public static final String CNF_COLLECTION_LOCATION = "c3po.collection.location";

  /**
   * Experimental property allowing to infer the date of tshe objects, if their
   * file names have a specific format.
   */
  public static final String CNF_INFER_DATE = "adaptor.inference.date";

  /**
   * A collection identifier configuration for the adaptors.s
   */
  public static final String CNF_COLLECTION_ID = "adaptor.collection.identifier";

  /**
   * A configuartion for recursive processing.
   */
  public static final String CNF_RECURSIVE = "c3po.recursive";
  
  /**
   * A configuration for processing with extraction.
   */
  public static final String CNF_EXTRACT = "c3po.extract";

  /**
   * The thread count configuration during meta data harvesting.
   */
  public static final String CNF_THREAD_COUNT = "c3po.thread.count";

  /**
   * The hostname of the server where the db is running.
   */
  public static final String CNF_DB_HOST = "db.host";

  /**
   * The port of the server where the db is listening to.
   */
  public static final String CBF_DB_PORT = "db.port";

  /**
   * The database name.
   */
  public static final String CNF_DB_NAME = "db.name";

  /**
   * A javascript Map function for building a histogram of a specific property.
   * All occurrences of that property are used (if they do not have conflcited
   * values). Note that there is a '{}' wildcard that has to be replaced with
   * the id of the desired property, prior to usage.
   */
  public static final String HISTOGRAM_MAP = "function map() {if (this.metadata['{}'] != null) {if (this.metadata['{}'].status !== 'CONFLICT') {emit(this.metadata['{}'].value, 1);}else{emit('Conflicted', 1);}} else {emit('Unknown', 1);}}";

  /**
   * A javascript Map function for building a histogram over a specific date
   * property. All occurrences of that property are used. If they are conflicted
   * then they are aggregated under one key 'Conflcited'. If the property is
   * missing, then the values are aggregated under the key 'Unknown'. Otherwise
   * the year is used as the key. Note that there is a '{}' wildcard that has to
   * be replaced with the id of the desired property, prior to usage.
   */
  public static final String DATE_HISTOGRAM_MAP = "function () {if (this.metadata['{}'] != null) {if (this.metadata['{}'].status !== 'CONFLICT') {emit(this.metadata['{}'].value.getFullYear(), 1);}else{emit('Conflicted', 1);}}else{emit('Unknown', 1);}}";

  /**
   * A javascript Map function for building a histogram with fixed bin size. It
   * takes two wilde cards as parameters - The {1} is the numeric property and
   * the {2} is the bin size. The result contains the bins, where the id is from
   * 0 to n and the value is the number of occurrences. Note that each bin has a
   * fixed size so the label can be easily calculated. For example the id 0
   * marks the number of elements where the numeric property was between 0 and
   * the width, the id 1 marks the number of elements where the numeric property
   * was between the width and 2*width and so on.
   */
  public static final String NUMERIC_HISTOGRAM_MAP = "function () {if (this.metadata['{1}'] != null) {if (this.metadata['{1}'].status !== 'CONFLICT') {var idx = Math.floor(this.metadata['{1}'].value / {2});emit(idx, 1);} else {emit('Conflicted', 1);}}else{emit('Unknown', 1);}}";

  /**
   * The reduce function for the {@link Constants#HISTOGRAM_MAP}.
   */
  public static final String HISTOGRAM_REDUCE = "function reduce(key, values) {var res = 0;values.forEach(function (v) {res += v;});return res;}";

  /**
   * A javascript Map function for calculating the min, max, sum, avg, sd and
   * var of a numeric property. Note that there is a wildcard {1} that has to be
   * replaced with the id of the desired numeric property prior to usage.
   */
  public static final String AGGREGATE_MAP = "function map() {emit(1,{sum: this.metadata['{1}'].value, min: this.metadata['{1}'].value,max: this.metadata['{1}'].value,count:1,diff: 0,});}";

  /**
   * The same as {@link Constants#AGGREGATE_MAP} but it aggregates the desired
   * property only for elements where the passed filter has a specific value.
   * {1} - the filter property id (e.g. 'mimetype') {2} - the value of the
   * filter (e.g. 'application/pdf') {3} - the property to aggregate (e.g.
   * 'size')
   */
  public static final String FILTER_AGGREGATE_MAP = "function map() {if (this.metadata['{1}'].value === '{2}') {emit(1,{sum: this.metadata['{3}'].value, min: this.metadata['{3}'].value,max: this.metadata['{3}'].value,count:1,diff: 0,});}}";

  /**
   * The reduce of the aggregation functions.
   */
  public static final String AGGREGATE_REDUCE = "function reduce(key, values) {var a = values[0];for (var i=1; i < values.length; i++){var b = values[i];var delta = a.sum/a.count - b.sum/b.count;var weight = (a.count * b.count)/(a.count + b.count);a.diff += b.diff + delta*delta*weight;a.sum += b.sum;a.count += b.count;a.min = Math.min(a.min, b.min);a.max = Math.max(a.max, b.max);}return a;}";

  /**
   * A finalize function for the aggregation map reduce job, to calculate the
   * average, standard deviation and variance.
   */
  public static final String AGGREGATE_FINALIZE = "function finalize(key, value){ value.avg = value.sum / value.count;value.variance = value.diff / value.count;value.stddev = Math.sqrt(value.variance);return value;}";
  
  
  public static final String PROPERTIES_IN_COLLECTION_MAP = "function map() {for (var key in this) {if (key == 'metadata') {for (var subkey in this[key]) {emit(subkey, null);}}}}";
  
  public static final String PROPERTIES_IN_COLLECTION_REDUCE = "function reduce(key, values) {return null;}";

  // "function reduce(key, values) {var res = {count: 0}; values.forEach(function (v) {res.count += v.count}); return res;}";

  private Constants() {

  }
}
