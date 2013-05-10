package com.petpet.c3po.parameters.validation;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;
import com.petpet.c3po.analysis.RepresentativeAlgorithmFactory;

public class SampleAlgorithmValidator implements IValueValidator<String> {

  @Override
  public void validate(String name, String value) throws ParameterException {
    if (!RepresentativeAlgorithmFactory.isValidAlgorithm(name)) {
      throw new ParameterException("Algorithm " + name + " is not supported");
    }
  }

}
