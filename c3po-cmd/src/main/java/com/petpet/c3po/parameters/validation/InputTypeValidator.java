package com.petpet.c3po.parameters.validation;

import java.util.Arrays;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

/**
 * Checks that the passed meta data input type is supported.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class InputTypeValidator implements IValueValidator<String> {

  /**
   * The supported metadata types.
   */
  private static final String[] SUPPORTED_METADATA = { "FITS", "TIKA" };

  /**
   * @throws ParameterException
   *           if the passed value is not supported.
   */
  @Override
  public void validate( String name, String value ) throws ParameterException {
    if ( !Arrays.asList( SUPPORTED_METADATA ).contains( value ) ) {
      throw new ParameterException( "Input type '" + value + "' is not supported. Please use one of "
          + Arrays.deepToString( SUPPORTED_METADATA ) );
    }

  }

}
