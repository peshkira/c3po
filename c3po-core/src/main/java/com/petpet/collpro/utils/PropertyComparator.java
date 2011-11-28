package com.petpet.collpro.utils;

import java.util.Comparator;

import com.petpet.collpro.datamodel.Property;

public class PropertyComparator implements Comparator<Property> {

  @Override
  public int compare(Property p1, Property p2) {
    return p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase());
  }

}
