package com.petpet.c3po.command;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.controller.Controller;
import com.petpet.c3po.parameters.GatherParams;
import com.petpet.c3po.parameters.Params;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POConfigurationException;

public class GatherCommand extends AbstractCLICommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(GatherCommand.class);

  private GatherParams params;

  @Override
  public void execute() {
    LOG.info("Starting meta data gathering command.");
    long start = System.currentTimeMillis();

    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();

    final Map<String, String> conf = new HashMap<String, String>();
    conf.put(Constants.OPT_COLLECTION_LOCATION, this.params.getLocation());
    conf.put(Constants.OPT_COLLECTION_NAME, this.params.getCollection());
    conf.put(Constants.OPT_INPUT_TYPE, this.params.getType());
    conf.put(Constants.OPT_RECURSIVE, this.params.isRecursive() + "");

    final Controller ctrl = new Controller(configurator);
    try {
      ctrl.processMetaData(conf);
    } catch (C3POConfigurationException e) {
      LOG.error(e.getMessage());
      return;
      
    } finally {
      cleanup();
    }
    

    long end = System.currentTimeMillis();
    this.setTime(end - start);
  }

  @Override
  public void setDelegateParams(Params params) {
    if (params != null && params instanceof GatherParams) {
      this.params = (GatherParams) params;
    }
  }

}
