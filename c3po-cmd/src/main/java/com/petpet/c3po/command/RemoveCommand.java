package com.petpet.c3po.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.controller.Controller;
import com.petpet.c3po.parameters.Params;
import com.petpet.c3po.parameters.RemoveParams;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POConfigurationException;

public class RemoveCommand extends AbstractCLICommand {

  private static final Logger LOG = LoggerFactory.getLogger(RemoveCommand.class);

  private RemoveParams params;

  @Override
  public void setDelegateParams(Params params) {
    if (params != null && params instanceof RemoveParams) {
      this.params = (RemoveParams) params;
    }
  }

  @Override
  public void execute() {
    String collection = this.params.getCollection();
    boolean proceed = this.prompt(collection);
    if (!proceed) {
      System.out.println("Oh, my! Collection names do not match.\nStopping collection removal.");
      return;
    }

    final long start = System.currentTimeMillis();

    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();

    Controller ctrl = new Controller(configurator);

    Map<String, Object> options = new HashMap<String, Object>();
    options.put(Constants.OPT_COLLECTION_NAME, collection);

    try {
      ctrl.removeCollection(options);
    } catch (C3POConfigurationException e) {
      LOG.error(e.getMessage());
      return;

    } finally {
      cleanup();
    }

    final long end = System.currentTimeMillis();
    this.setTime(end - start);
  }

  private boolean prompt(String name) {
    System.out.println("Are you sure you want to remove all elements from collection " + name
        + "?\nPlease type in the collection name again and hit Enter!");
    Scanner scanner = new Scanner(System.in);
    String next = scanner.next();

    return (next.equals(name));
  }

}
