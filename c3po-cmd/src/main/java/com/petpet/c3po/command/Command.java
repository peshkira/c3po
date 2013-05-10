package com.petpet.c3po.command;

import com.petpet.c3po.parameters.Params;

public interface Command {

  void setDelegateParams(Params params);
  
  void execute();

  long getTime();
}
