package com.petpet.c3po.utils.exceptions;

/**
 * A simple exception that is thrown if something goes wrong in the persistence
 * layer.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class C3POPersistenceException extends Exception {

  /**
   * a generated uid.
   */
  private static final long serialVersionUID = -7832181913915584099L;

  /**
   * {@inheritDoc}
   */
  public C3POPersistenceException() {
    super();
  }

  /**
   * {@inheritDoc}
   * 
   * @param msg
   *          the message to supply
   */
  public C3POPersistenceException(String msg) {
    super( msg );
  }

  /**
   * {@inheritDoc}
   * 
   * @param msg
   *          the message to supply
   * @param cause
   *          the cause for this exception.
   */
  public C3POPersistenceException(String msg, Throwable cause) {
    super( msg, cause );
  }

  /**
   * {@inheritDoc}
   * 
   * @param cause
   *          the cause for this exception.
   */
  public C3POPersistenceException(Throwable cause) {
    super( cause );
  }

}
