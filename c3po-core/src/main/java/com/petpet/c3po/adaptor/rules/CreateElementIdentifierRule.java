package com.petpet.c3po.adaptor.rules;

import java.util.UUID;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.utils.Configurator;

/**
 * A {@link PostProcessingRule} that creates an element uid if it was not
 * created by adaptor for some reason. This rule is turned on per default and
 * can be disabled via the .c3poconfig file with the following key set to false:
 * 'c3po.rule.create_element_identifier'
 * 
 * @see Configurator
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class CreateElementIdentifierRule implements PostProcessingRule {

  /**
   * This rule has a low priority.
   */
  @Override
  public int getPriority() {
    return 10;
  }

  /**
   * Sets a random uuid if the element has no uid defined.
   */
  @Override
  public Element process(Element e) {
    if (e != null && e.getUid() == null) {
      e.setUid(UUID.randomUUID().toString());
    }

    return e;
  }

}
