package com.petpet.c3po.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.datamodel.BooleanValue;
import com.petpet.c3po.datamodel.FloatValue;
import com.petpet.c3po.datamodel.IntegerValue;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.PropertyType;
import com.petpet.c3po.datamodel.StringValue;
import com.petpet.c3po.datamodel.Value;

public final class Helper {

  private static final Logger LOG = LoggerFactory.getLogger(Helper.class);

  private static Properties TYPES;

  /**
   * A map with the known properties. It is populated by the configurator
   * usually at startup.
   */
  public static Map<String, Property> KNOWN_PROPERTIES = new HashMap<String, Property>();

  private Helper() {

  }

  public static void init() {
    try {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("known.properties");
      TYPES = new Properties();
      TYPES.load(in);
      in.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static <T extends Value<?>> T getTypedValue(Class<T> type, String value) {

    try {
      Constructor<T> constr = type.getConstructor(String.class);
      return constr.newInstance(value);

    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

    return null;
  }
  
  public static Value<?> getTypedValue(PropertyType type, String value) {
    Value<?> v = null;
    switch(type) {
      case BOOL:
        v = new BooleanValue(value);
        break;
      case FLOAT:
        v = new FloatValue(value);
        break;
      case NUMERIC:
        v = new IntegerValue(value);
        break;
      case ARRAY:
      case STRING:
      case DEFAULT:
        v = new StringValue(value);
        break;
    }
    
    return v;
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
      } else {
        LOG.warn("Prop {} is unknown, this should be fixed", n);
      }
    }

    return result;
  }

  // FIXME DAO
  // public static boolean isElementAlreadyProcessed(DigitalCollection coll,
  // String md5) {
  // if (md5 == null || md5.equals("")) {
  // LOG.warn("No checksum provided, assuming element is not processed.");
  // return false;
  // }
  //
  // boolean isDone = false;
  // LOG.debug("MD5: {}", md5);
  //
  // try {
  // DBManager.getInstance().getEntityManager().createNamedQuery(Constants.COLLECTION_VALUES_BY_NAME_AND_VALUE)
  // .setParameter("pname", "checksum.md5").setParameter("value",
  // md5).setParameter("coll", coll)
  // .getSingleResult();
  // isDone = true;
  // } catch (NoResultException nre) {
  // LOG.debug("No element with this checksum ingested, continue processing.");
  // isDone = false;
  //
  // } catch (NonUniqueResultException nue) {
  // LOG.warn("More than one elements with this checksum are already processed. Please inspect");
  // isDone = true;
  // }
  //
  // return isDone;
  // }
}
