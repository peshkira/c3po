package com.petpet.c3po.dao;

import java.util.Comparator;

import com.petpet.c3po.api.model.helper.MetadataRecord;

/**
 * A {@link Comparator}, used to sort {@link MetadataRecord}s by their property
 * id and value.
 */
public class MetadataSorter implements Comparator<MetadataRecord> {

  private static MetadataSorter SINGLETON = new MetadataSorter();

  private MetadataSorter() {

  }

  public static MetadataSorter getInstance() {
    return SINGLETON;
  }

  @Override
  public int compare(MetadataRecord o1, MetadataRecord o2) {
    int compare = o1.getProperty().compareTo(o2.getProperty());
    if (compare != 0) {
      return compare;
    }

    compare = 0;//o1.getValues().compareTo(o2.getValues());
    if (compare != 0) {
      return compare;
    }

    return o1.hashCode() - o2.hashCode();

  }
}
