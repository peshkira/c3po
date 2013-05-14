package com.petpet.c3po.utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.MetadataRecord.Status;
import com.petpet.c3po.api.model.helper.PropertyType;

public final class DataHelper {

  private static final Logger LOG = LoggerFactory.getLogger(DataHelper.class);

  private static Properties TYPES;

  /**
   * Some date patterns used for date parsing.
   */
  private static final String[] PATTERNS = { "yyyy:MM:dd HH:mm:ss", "MM/dd/yyyy HH:mm:ss", "dd MMM yyyy HH:mm",
      "EEE dd MMM yyyy HH:mm", "EEE, MMM dd, yyyy hh:mm:ss a", "EEE, MMM dd, yyyy hh:mm a", "EEE dd MMM yyyy HH.mm",
      "HH:mm MM/dd/yyyy", "yyyyMMddHHmmss", "yyyy-MM-dd'T'HH:mm:ss" };

  public static void init() {
    try {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("datatypes.properties");
      TYPES = new Properties();
      TYPES.load(in);
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getPropertyType(String key) {
    return TYPES.getProperty(key, "STRING");
  }

  public static String removeTrailingZero(final String str) {
    if (str != null && str.endsWith(".0")) {
      return str.substring(0, str.length() - 2);
    }

    return str;
  }

  public static void mergeMetadataRecord(Element e, MetadataRecord mr) {

    if (e == null || mr == null) {
      return;
    }

    List<MetadataRecord> oldMetadata = e.removeMetadata(mr.getProperty().getId());
    if (oldMetadata.size() == 0) {

      e.getMetadata().add(mr);

    } else if (oldMetadata.size() == 1) {

      MetadataRecord oldMR = oldMetadata.get(0);
      if (oldMR.getStatus().equals(Status.CONFLICT.name())) {
        
        String newVal = getTypedValue(mr.getProperty().getType(), mr.getValue()).toString();
        if (!oldMR.getValues().contains(newVal)) {
          mr.setStatus(Status.CONFLICT.name());
          oldMetadata.add(mr);
        }
        
      } else {
        String oldVal = getTypedValue(oldMR.getProperty().getType(), oldMR.getValue()).toString();
        String newVal = getTypedValue(mr.getProperty().getType(), mr.getValue()).toString();

        if (!oldVal.equals(newVal)) {
          oldMR.setStatus(Status.CONFLICT.name());
          mr.setStatus(Status.CONFLICT.name());
          oldMetadata.add(mr);
        }
      }
    } else {

      boolean exists = false;
      for (MetadataRecord old : oldMetadata) {
        String oldVal = getTypedValue(old.getProperty().getType(), old.getValue()).toString();
        String newVal = getTypedValue(mr.getProperty().getType(), mr.getValue()).toString();
        if (oldVal.equals(newVal)) {
          exists = true;
        }
      }

      if (!exists) {
        mr.setStatus(Status.CONFLICT.name());
        oldMetadata.add(mr);
      }
    }

    e.getMetadata().addAll(oldMetadata);
  }

//  @Deprecated
//  public static BasicDBObject getFilterQuery(Filter filter) {
//    PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
//    BasicDBObject ref = new BasicDBObject("descriminator", filter.getDescriminator());
//    ref.put("collection", filter.getCollection());
//    DBCursor cursor = pl.find(Constants.TBL_FILTERS, ref);
//
//    BasicDBObject query = new BasicDBObject("collection", filter.getCollection());
//
//    Filter tmp;
//    while (cursor.hasNext()) {
//      DBObject next = cursor.next();
//      tmp = DataHelper.parseFilter(next);
//      if (tmp.getValue() != null) {
//
//        Property property = pl.getCache().getProperty(tmp.getProperty());
//
//        if (tmp.getValue().equals("Unknown")) {
//          query.put("metadata." + tmp.getProperty() + ".values", new BasicDBObject("$exists", false));
//          query.put("metadata." + tmp.getProperty() + ".value", new BasicDBObject("$exists", false));
//
//        } else if (tmp.getValue().equals("Conflicted")) {
//          query.put("metadata." + tmp.getProperty() + ".status", MetadataRecord.Status.CONFLICT.toString());
//
//        } else if (property.getType().equals(PropertyType.DATE.toString())) {
//
//          Calendar cal = Calendar.getInstance();
//          cal.set(Integer.parseInt(tmp.getValue()), Calendar.JANUARY, 1);
//          Date start = cal.getTime();
//          cal.set(Integer.parseInt(tmp.getValue()), Calendar.DECEMBER, 31);
//          Date end = cal.getTime();
//
//          BasicDBObject date = new BasicDBObject();
//          date.put("$lte", end);
//          date.put("$gte", start);
//
//          query.put("metadata." + tmp.getProperty() + ".value", date);
//
//        } else if (property.getType().equals(PropertyType.INTEGER.toString())) {
//          String val = tmp.getValue();
//          String[] constraints = val.split(" - ");
//          String low = constraints[0];
//          String high = constraints[1];
//
//          BasicDBObject range = new BasicDBObject();
//          range.put("$lte", Long.parseLong(high));
//          range.put("$gte", Long.parseLong(low));
//
//          query.put("metadata." + tmp.getProperty() + ".value", range);
//
//        } else {
//          query.put("metadata." + tmp.getProperty() + ".value", inferValue(tmp.getValue()));
//        }
//      }
//    }
//
//    LOG.debug("FILTER QUERY: {}", query.toString());
//    return query;
//  }

//  private static Object inferValue(String value) {
//    Object result = value;
//    if (value.equalsIgnoreCase("true")) {
//      result = new Boolean(true);
//    }
//
//    if (value.equalsIgnoreCase("false")) {
//      result = new Boolean(false);
//    }
//
//    return result;
//  }

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
  public static Object getTypedValue(String t, String value) {

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
        result = getBooleanValue(value);
        break;
      case INTEGER:
        result = getIntegerValue(value);
        break;
      case FLOAT:
        result = getDoubleValue(value);
        break;
      case DATE:
        result = getDateValue(value);
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
  private static Date getDateValue(String value) {
    LOG.trace("parsing value {} as date", value);

    final SimpleDateFormat fmt = new SimpleDateFormat();

    Date result = null;
    for (String p : PATTERNS) {

      fmt.applyPattern(p);
      result = parseDate(fmt, value);

      if (result != null) {
        break;
      }
    }

    if (result == null) {
      LOG.trace("No pattern matching for value {}, try to parse as long", value);
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
  private static Double getDoubleValue(String value) {
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
  private static Long getIntegerValue(String value) {
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
  private static Boolean getBooleanValue(String value) {
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
  private static Date parseDate(DateFormat fmt, String d) {
    try {
      return fmt.parse(d);
    } catch (ParseException e) {
      LOG.trace("date could not be parsed: {}", e.getMessage());
      return null;
    }
  }

  private DataHelper() {

  }
}
