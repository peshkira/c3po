package com.petpet.c3po.parameters.validation;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;
import com.petpet.c3po.analysis.RepresentativeAlgorithmFactory;

/**
 * Checks wheter the passed value is a supported representative selection
 * algorithm.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class SampleAlgorithmValidator implements IValueValidator<String> {

  /**
   * @throws ParameterException
   *           if the value is not a valid algorithm.
   */
  @Override
  public void validate( String name, String value ) throws ParameterException {
    if ( !RepresentativeAlgorithmFactory.isValidAlgorithm( name ) ) {
      throw new ParameterException( "Algorithm " + name + " is not supported" );
    }
  }

}
