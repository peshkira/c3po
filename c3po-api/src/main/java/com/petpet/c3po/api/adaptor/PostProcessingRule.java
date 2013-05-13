package com.petpet.c3po.api.adaptor;

import com.petpet.c3po.api.model.Element;

/**
 * A post processing rule gets a parsed element and does some processing on the
 * given element. It can modify the element as it sees fit. All active post
 * processing rules are applied after the parsing according to their respective
 * priority.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
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
