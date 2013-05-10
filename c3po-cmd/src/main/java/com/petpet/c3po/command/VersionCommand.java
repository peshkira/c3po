package com.petpet.c3po.command;

import com.petpet.c3po.C3PO;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.parameters.Params;

public class VersionCommand implements Command {

  @Override
  public void execute() {
    System.out.println("I am c3po, human content profiling relations!");
    System.out.println("c3po-cmd: " + C3PO.VERSION);
    System.out.println("c3po-core: " + Constants.CORE_VERSION);
    System.out.println("c3po-api: " + Constants.API_VERSION);

  }

  @Override
  public long getTime() {
    return -1;
  }

  @Override
  public void setDelegateParams(Params params) {
    
  }

}
