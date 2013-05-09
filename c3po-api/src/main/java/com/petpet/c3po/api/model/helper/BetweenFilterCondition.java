package com.petpet.c3po.api.model.helper;

public class BetweenFilterCondition extends FilterCondition {
  
  public enum Operator {
    GT, GTE, LT, LTE
  }
  
  private Operator lOperator;
  
  private Operator hOperator;
  
  private Object lValue;
  
  private Object hValue;
  
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
