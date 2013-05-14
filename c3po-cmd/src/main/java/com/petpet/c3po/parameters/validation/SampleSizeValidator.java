package com.petpet.c3po.parameters.validation;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

/**
 * Checks that the size value is > 0.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class SampleSizeValidator implements IValueValidator<Integer> {

  /**
   * @throws ParameterException
   *           if the given value is less than or equal to 0.
   */
  @Override
  public void validate( String name, Integer val ) throws ParameterException {
    if ( val <= 0 ) {
      throw new ParameterException( "The sample size cannot be less than or equal to 0" );
    }
  }

}
