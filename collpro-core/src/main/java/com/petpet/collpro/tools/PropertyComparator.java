package com.petpet.collpro.tools;

import java.util.Comparator;

import com.petpet.collpro.datamodel.Property;

public class PropertyComparator implements Comparator<Property> {

  @Override
  public int compare(Property o1, Property o2) {
    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
  }

}
