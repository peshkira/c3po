package com.petpet.c3po.api.model.helper;

/**
 * A filter condition that represent a between x and y condition, e.g. pagecound
 * between GT 5 and LTE 42.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class BetweenFilterCondition extends FilterCondition {

  /**
   * The different supported operators.
   * 
   * @author Petar Petrov <me@petarpetrov.org>
   * 
   */
  public enum Operator {
    /**
     * Greater than.
     */
    GT,

    /**
     * Greater than equal.
     */
    GTE,

    /**
     * Less than.
     */
    LT,

    /**
     * Less than equal.
     */
    LTE
  }

  /**
   * The operator for the lower bound.
   */
  private Operator lOperator;

  /**
   * The operator for the higher bound.
   */
  private Operator hOperator;

  /**
   * The value for the lower bound.
   */
  private Object lValue;

  /**
   * The value for the higher bound.
   */
  private Object hValue;

  /**
   * Creates a between filter condition.
   * 
   * @param f
   *          the property that is going to be filtered.
   * @param lOp
   *          the lower bound operator.
   * @param lVal
   *          the lower bound value.
   * @param hOp
   *          the higher bound operator.
   * @param hVal
   *          the higher bound operator.
   */
  public BetweenFilterCondition(String f, Operator lOp, Object lVal, Operator hOp, Object hVal) {
    this.setProperty(f);
    this.lOperator = lOp;
    this.hOperator = hOp;
    this.lValue = lVal;
    this.hValue = hVal;

  }

  public void setLOperator(Operator lOp) {
    this.lOperator = lOp;
  }

  public Operator getLOperator() {
    return this.lOperator;
  }

  public void setHOperator(Operator hOp) {
    this.hOperator = hOp;
  }

  public Operator getHOperator() {
    return this.hOperator;
  }

  public void setLValue(Object lVal) {
    this.lValue = lVal;
  }

  public Object getLValue() {
    return this.lValue;
  }

  public void setHValue(Object hVal) {
    this.hValue = hVal;
  }

  public Object getHValue() {
    return this.hValue;
  }

}
