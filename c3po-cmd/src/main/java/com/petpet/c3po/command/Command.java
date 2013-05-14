package com.petpet.c3po.command;

import com.petpet.c3po.parameters.Params;

/**
 * A command interface for the CLI. It denotes a mode in which the C3PO CLI can
 * operate.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface Command {

  /**
   * Sets the parameter for the command (that were passed on the command line).
   * 
   * @param params
   *          the params to set.
   */
  void setParams( Params params );

  /**
   * Called if everything was ok and should execute the current command. Note
   * that the {@link Command#setParams(Params)} method will be called exactly
   * once before calling this method. The method should optionally record its
   * time for execution (where needed) and return it in
   * {@link Command#getTime()}.
   */
  void execute();

  /**
   * Retrieves the time needed for the execution of this command in
   * milliseconds. If the command does not require time tracking, then -1 is
   * returned.
   * 
   * @return the time needed for this command to execute.
   */
  long getTime();
}
