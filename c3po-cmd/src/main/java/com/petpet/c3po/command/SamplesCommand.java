package com.petpet.c3po.command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.controller.Controller;
import com.petpet.c3po.parameters.Params;
import com.petpet.c3po.parameters.SamplesParams;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POConfigurationException;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;

public class SamplesCommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(SamplesCommand.class);

  private SamplesParams params;

  private long time = -1L;

  @Override
  public void setDelegateParams(Params params) {
    if (params != null && params instanceof SamplesParams) {
      this.params = (SamplesParams) params;
    }
  }

  @Override
  public void execute() {
    long start = System.currentTimeMillis();

    Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();

    Map<String, Object> options = new HashMap<String, Object>();
    options.put(Constants.OPT_COLLECTION_NAME, this.params.getCollection());
    options.put(Constants.OPT_OUTPUT_LOCATION, this.params.getLocation());
    options.put(Constants.OPT_SAMPLING_ALGORITHM, this.params.getAlgorithm());
    options.put(Constants.OPT_SAMPLING_SIZE, this.params.getSize());
    options.put(Constants.OPT_SAMPLING_PROPERTIES, this.params.getProperties());

    Controller ctrl = new Controller(configurator);
    try {
      List<String> samples = ctrl.findSamples(options);
      if (samples.size() == 0) {
        System.out.println("Oh, my! I did not find any samples");

      } else {
        String location = this.params.getLocation();
        if (location == null) {
          print(samples);
        } else {
          try {
            File file = new File(location + File.separator + "samples.txt");

            if (!file.exists()) {
              file.getParentFile().mkdirs();
              file.createNewFile();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (String sample : samples) {
              writer.append(sample + "\n");
            }

            writer.flush();
            writer.close();
          } catch (IOException e) {
            LOG.warn("An error occurred: {}. Outputting to stdout", e.getMessage());
            print(samples);
          }
        }
      }

    } catch (C3POConfigurationException e) {
      LOG.error(e.getMessage());
      return; //still executes finally :)

    } finally {
      cleanup();
    }

    long end = System.currentTimeMillis();
    this.time = end - start;
  }

  @Override
  public long getTime() {
    return this.time;
  }

  private void print(List<String> samples) {
    for (String sample : samples) {
      System.out.println(sample);
    }
  }

  private void cleanup() {
    try {
      Configurator.getDefaultConfigurator().getPersistence().close();
    } catch (C3POPersistenceException e) {
      LOG.error(e.getMessage());
    }
  }

}
