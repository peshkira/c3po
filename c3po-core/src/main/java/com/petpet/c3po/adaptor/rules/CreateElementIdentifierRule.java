package com.petpet.c3po.adaptor.rules;

import java.util.UUID;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;

public class CreateElementIdentifierRule implements PostProcessingRule {

  @Override
  public int getPriority() {
    return 10;
  }

  @Override
  public Element process(Element e) {
    if (e != null && e.getUid() == null) {
      e.setUid(UUID.randomUUID().toString());
    }

    return e;
  }

}
