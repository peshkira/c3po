package com.petpet.c3po.command;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class HelpCommand implements Command {

  private Options options;

  public HelpCommand(final Options o) {
    this.options = o;
  }

  @Override
  public void execute() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java c3po", this.options);
  }

  @Override
  public long getTime() {
    return -1L;
  }

}
