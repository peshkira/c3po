package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.datamodel.Element;

public interface PostProcessingRule extends ProcessingRule {

  /**
   * This method does some processing over the passed element and returns it. It
   * can do any kind of post processing to the element.
   * 
   * @param e
   *          the element to process.
   * @return the processed element.
   */
  Element process(Element e);

}
