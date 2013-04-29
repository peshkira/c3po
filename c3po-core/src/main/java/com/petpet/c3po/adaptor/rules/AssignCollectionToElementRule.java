package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;

public class AssignCollectionToElementRule implements PostProcessingRule {

  private String collectionName;

  public AssignCollectionToElementRule(String name) {
    this.collectionName = name;
  }

  @Override
  public int getPriority() {
    return 990;
  }

  @Override
  public Element process(Element e) {
    if (e != null) {
      e.setCollection(this.collectionName);
    }

    return e;
  }

}
