package com.petpet.c3po.parameters.validation;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

/**
 * A empty string validator for the passed command line parameters.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class EmptyStringValidator implements IValueValidator<String> {

  /**
   * @throws ParameterException
   *           if the value is null or an empty string.
   */
  @Override
  public void validate( String name, String val ) throws ParameterException {
    if ( val == null || val.equals( "" ) ) {
      throw new ParameterException( "The value of the " + name + " option cannot be empty" );
    }
  }

}
