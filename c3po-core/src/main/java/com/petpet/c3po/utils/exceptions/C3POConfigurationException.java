package com.petpet.c3po.utils.exceptions;

/**
 * A simple exception to be thrown when a (internal) configuration error
 * occurrs.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class C3POConfigurationException extends Exception {

  /**
   * a generated uid.
   */
  private static final long serialVersionUID = 1961422857090339591L;

  public C3POConfigurationException() {
    super();
  }

  public C3POConfigurationException(final String msg) {
    super(msg);
  }

  public C3POConfigurationException(final String msg, final Throwable cause) {
    super(msg, cause);
  }

  public C3POConfigurationException(final Throwable cause) {
    super(cause);
  }
}
