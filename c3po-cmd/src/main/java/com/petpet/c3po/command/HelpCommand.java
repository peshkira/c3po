package com.petpet.c3po.command;

import java.util.Map;

import com.beust.jcommander.JCommander;
import com.petpet.c3po.parameters.Params;

public class HelpCommand implements Command {

  private Map<String, Params> params;

  public HelpCommand(Map<String, Params> params) {
    this.params = params;
  }

  @Override
  public void execute() {
    for (String mode : params.keySet()) {
      if (!mode.equals("help")) {
        JCommander jc = new JCommander(params.get(mode));
        jc.setProgramName("c3po " + mode);
        jc.usage();
      }
    }
  }

  @Override
  public long getTime() {
    return -1L;
  }

  @Override
  public void setDelegateParams(Params params) {

  }

}
