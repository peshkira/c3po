package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.dao.ReadOnlyCache;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.common.Constants;

public class InferDateFromFileNameRule implements PostProcessingRule {

  private ReadOnlyCache cache;

  public InferDateFromFileNameRule(ReadOnlyCache cache) {
    this.cache = cache;
  }

  @Override
  public int getPriority() {
    return 10;
  }

  @Override
  public Element process(Element e) {
    if (e != null) {
      this.extractCreatedMetadataRecord(e, this.cache.getProperty("created"));
    }

    return e;
  }
  
  /*
   * experimental only for the SB archive
   */
  /**
   * Tries to parse a creation date out of the name of the current element. Some
   * sources include a timestamp in the name. 
   * 
   * If the name is not set or it does not include a timestamp, the method does
   * nothing.
   * 
   * @param created
   *          the property for the creation date.
   */
  private void extractCreatedMetadataRecord(Element e, Property created) {
    String name = e.getName();
    if (name != null) {

      String[] split = name.split("-");

      if (split.length > 2) {
        String date = split[2];

        try {
          Long.valueOf(date);

          MetadataRecord c = new MetadataRecord(created, date);
          e.getMetadata().add(c);

        } catch (NumberFormatException nfe) {
          // if the value is not a number then it is something else and not a
          // year, skip the inference.

        }
      }
    }
  }

}
