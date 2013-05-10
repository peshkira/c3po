package com.petpet.c3po.command;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.controller.Controller;
import com.petpet.c3po.parameters.Params;
import com.petpet.c3po.parameters.ProfileParams;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POConfigurationException;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

public class ProfileCommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(ProfileCommand.class);

  private long time = -1L;
  private ProfileParams params;
  
  public ProfileCommand() {
    
  }

  @Override
  public void execute() {
    final long start = System.currentTimeMillis();

    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();

    Map<String, Object> options = new HashMap<String, Object>();
    options.put(Constants.OPT_COLLECTION_NAME, this.params.getCollection());
    options.put(Constants.OPT_OUTPUT_LOCATION, this.params.getLocation());
    options.put(Constants.OPT_SAMPLING_ALGORITHM, this.params.getAlgorithm());
    options.put(Constants.OPT_SAMPLING_SIZE, this.params.getSize());
    options.put(Constants.OPT_SAMPLING_PROPERTIES, this.params.getProperties());
    options.put(Constants.OPT_INCLUDE_ELEMENTS, this.params.isIncludeElements());
    
    Controller ctrl = new Controller(configurator);
    
    try {
      ctrl.profile(options);
    } catch (C3POConfigurationException e) {
      LOG.error(e.getMessage());
    }
    
    try {
      configurator.getPersistence().close();
    } catch (C3POPersistenceException e) {
      LOG.error(e.getMessage());
    }
    
    final long end = System.currentTimeMillis();
    this.time = end - start;
  }

  @Override
  public long getTime() {
    return this.time;
  }

  @Override
  public void setDelegateParams(Params params) {
    if (params != null && params instanceof ProfileParams) {
      this.params = (ProfileParams) params;
    }
    
  }

}
