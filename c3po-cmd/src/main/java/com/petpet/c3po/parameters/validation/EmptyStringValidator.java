package com.petpet.c3po.parameters.validation;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

public class EmptyStringValidator implements IValueValidator<String> {

  @Override
  public void validate(String name, String val) throws ParameterException {
    if (val == null || val.equals("")) {
      throw new ParameterException("The value of the " + name + " option cannot be empty");
    }
  }

}
