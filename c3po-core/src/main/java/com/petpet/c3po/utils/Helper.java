package com.petpet.c3po.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.BooleanValue;
import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.datamodel.FloatValue;
import com.petpet.c3po.datamodel.IntegerValue;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.PropertyType;
import com.petpet.c3po.datamodel.StringValue;
import com.petpet.c3po.datamodel.Value;
import com.petpet.c3po.db.DBManager;

public final class Helper {

  private static final Logger LOG = LoggerFactory.getLogger(Helper.class);

  private static Properties TYPES;

  static {
    TYPES = new Properties();
    InputStream in;
    try {
      in = new FileInputStream("src/main/resources/known.properties");
      TYPES.load(in);
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * A map with the known properties. It is populated by the configurator
   * usually at startup.
   */
  public static Map<String, Property> KNOWN_PROPERTIES = new HashMap<String, Property>();

  private Helper() {

  }

  public static Value getTypedValue(PropertyType type, String value) {

    switch (type) {
    case ARRAY:
      return null;
    case BOOL:
      return new BooleanValue(value);
    case NUMERIC:
      return new IntegerValue(value);
    case FLOAT:
      return new FloatValue(value);
    case STRING: // empty on purpose
    case DEFAULT: // empty on purpose
    default:
      return new StringValue(value);
    }
  }

  public static PropertyType getType(String name) {
    String prop = (String) TYPES.get(name);
    String type = null;
    if (prop != null) {
      type = prop.split(",")[0];
    }
    return PropertyType.getTypeFromString(type);
  }

  public static Property getPropertyByName(String name) {
    Property p = Helper.KNOWN_PROPERTIES.get(name);

    if (p == null) {
      p = new Property();
      p.setName(name);
      p.setType(Helper.getType(p.getName()));

      String prop = (String) TYPES.get(name);
      if (prop != null) {
        String[] desc = prop.split(",");
        p.setHumanReadableName(desc[1]);
        if (desc.length > 2) {
          p.setDescription(desc[2]);
        }
      } else {
        p.setHumanReadableName(name);
      }

      Helper.KNOWN_PROPERTIES.put(p.getName(), p);
      // TODO eventually write it also the the properties file.
    }

    return p;
  }

  public static List<Property> getPropertiesByNames(String... names) {
    List<Property> result = new ArrayList<Property>();
    for (String n : names) {
      Property p = Helper.KNOWN_PROPERTIES.get(n);
      if (p != null) {
        result.add(p);
      }
    }

    return result;
  }

  public static boolean isElementAlreadyProcessed(DigitalCollection coll, String md5) {
    if (md5 == null || md5.equals("")) {
      LOG.warn("No checksum provided, assuming element is not processed.");
      return false;
    }

    boolean isDone = false;
    LOG.debug("MD5: {}", md5);

    try {
      DBManager.getInstance().getEntityManager().createNamedQuery(Constants.COLLECTION_VALUES_BY_NAME_AND_VALUE)
          .setParameter("pname", "checksum.md5").setParameter("value", md5).setParameter("coll", coll)
          .getSingleResult();
      isDone = true;
    } catch (NoResultException nre) {
      LOG.debug("No element with this checksum ingested, continue processing.");
      isDone = false;

    } catch (NonUniqueResultException nue) {
      LOG.warn("More than one elements with this checksum are already processed. Please inspect");
      isDone = true;
    }

    return isDone;
  }
}
