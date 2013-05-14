package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;

/**
 * This {@link PostProcessingRule} applies the given collection to every
 * {@link Element} that is processed.
 * 
 * Note that this rule is always turned on.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class AssignCollectionToElementRule implements PostProcessingRule {

  /**
   * The collection to apply.
   */
  private String collectionName;

  /**
   * Creates the rule.
   * 
   * @param name
   *          the name of the collection.
   */
  public AssignCollectionToElementRule(String name) {
    this.collectionName = name;
  }

  /**
   * Has a very high priority.
   */
  @Override
  public int getPriority() {
    return 990;
  }

  /**
   * Sets the collection of the given element to the colleciton name and returns
   * it.
   */
  @Override
  public Element process( Element e ) {
    if ( e != null ) {
      e.setCollection( this.collectionName );
    }

    return e;
  }

}
