package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.dao.ReadOnlyCache;
import com.petpet.c3po.api.model.Element;

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
      e.extractCreatedMetadataRecord(this.cache.getProperty("created"));
    }

    return e;
  }

}
