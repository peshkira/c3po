package com.petpet.c3po.adaptor.rules.drools;

import java.io.Serializable;
import java.util.Collection;

import org.drools.base.accumulators.CollectListAccumulateFunction;

public class UnionListAccumulateFunction extends CollectListAccumulateFunction {

  @Override
  public void accumulate(Serializable context, Object value) {
    for (Object item : (Collection<Object>) value) {
      super.accumulate(context, item);
    }
  }

  @Override
  public void reverse(Serializable context, Object value) throws Exception {
    for (Object item : (Collection<Object>) value) {
      super.reverse(context, item);
    }
  }
}
