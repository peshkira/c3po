package com.petpet.c3po.adaptor.rules.drools;

import java.io.Serializable;
import java.util.Collection;

import org.drools.base.accumulators.CollectSetAccumulateFunction;

public class UnionSetAccumulateFunction extends CollectSetAccumulateFunction {

  @Override
  public void accumulate(Serializable context, Object value) {
    if (value instanceof Collection<?>) {
      for (Object item : (Collection<?>) value) {
        super.accumulate(context, item);
      }
    }
  }

  @Override
  public void reverse(Serializable context, Object value) throws Exception {
    if (value instanceof Collection<?>) {
      for (Object item : (Collection<?>) value) {
        super.reverse(context, item);
      }
    }
  }
}
