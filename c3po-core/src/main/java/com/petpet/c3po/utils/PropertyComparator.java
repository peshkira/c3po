package com.petpet.c3po.utils;

import java.util.Comparator;

import com.petpet.c3po.datamodel.Property;

public class PropertyComparator implements Comparator<Property> {

  @Override
  public int compare(Property p1, Property p2) {
    return p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase());
  }

}
