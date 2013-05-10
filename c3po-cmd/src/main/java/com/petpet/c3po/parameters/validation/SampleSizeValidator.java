package com.petpet.c3po.parameters.validation;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

public class SampleSizeValidator implements IValueValidator<Integer> {

  @Override
  public void validate(String name, Integer val) throws ParameterException {
    if (val <= 0) {
      throw new ParameterException("The sample size cannot be less than or equal to 0");
    }
  }

}
