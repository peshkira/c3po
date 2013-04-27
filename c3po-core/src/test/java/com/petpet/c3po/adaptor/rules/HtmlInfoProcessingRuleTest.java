package com.petpet.c3po.adaptor.rules;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import com.petpet.c3po.api.model.Property;

public class HtmlInfoProcessingRuleTest {

  @Test
  public void shouldTestFaultyTagOccurrences() throws Exception {
    Property p1 = Mockito.mock(Property.class);
    Property p2 = Mockito.mock(Property.class);

    Mockito.when(p1.getKey()).thenReturn("bodyTagOccurences");
    Mockito.when(p2.getKey()).thenReturn("asdasdTagOccurences");

    HtmlInfoProcessingRule rule = new HtmlInfoProcessingRule();
    boolean skipOnValid = rule.shouldSkip(p1.getKey(), "whatever", "whatever", "HtmlInfo", "whatever");
    boolean skipOnFaulty = rule.shouldSkip(p2.getKey(), "whatever", "whatever", "HtmlInfo", "whatever");

    Assert.assertFalse(skipOnValid);
    Assert.assertTrue(skipOnFaulty);

  }
}
