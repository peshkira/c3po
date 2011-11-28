package com.petpet.c3po.datamodel;

public enum PropertyType {
  DEFAULT, BOOL, NUMERIC, STRING, ARRAY, FLOAT;

  public static PropertyType getTypeFromString(String t) {
    if (t == null) {
      return DEFAULT;
    }

    if (t.equals(BOOL.name())) {
      return BOOL;
    }

    if (t.equals(NUMERIC.name())) {
      return NUMERIC;
    }

    if (t.equals(STRING.name())) {
      return STRING;
    }

    if (t.equals(ARRAY.name())) {
      return ARRAY;
    }

    if (t.equals(FLOAT.name())) {
      return FLOAT;
    }

    return DEFAULT;
  }
}
