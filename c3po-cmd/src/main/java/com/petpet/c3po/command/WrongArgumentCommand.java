package com.petpet.c3po.command;

import org.apache.commons.cli.Options;

public class WrongArgumentCommand implements Command {

  private Options options;

  private String message;

  public WrongArgumentCommand(String msg, Options o) {
    this.message = msg;
    this.options = o;
  }

  @Override
  public void execute() {
    System.err.println("Sometimes I don't understand human behaviour [" + message + "]");
    new HelpCommand(this.options).execute();
  }

  @Override
  public long getTime() {
    return -1L;
  }

}
