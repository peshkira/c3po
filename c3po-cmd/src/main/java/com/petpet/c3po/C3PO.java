package com.petpet.c3po;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.petpet.c3po.command.Command;
import com.petpet.c3po.command.ExportCommand;
import com.petpet.c3po.command.GatherCommand;
import com.petpet.c3po.command.HelpCommand;
import com.petpet.c3po.command.ProfileCommand;
import com.petpet.c3po.command.VersionCommand;
import com.petpet.c3po.parameters.ExportParams;
import com.petpet.c3po.parameters.GatherParams;
import com.petpet.c3po.parameters.Params;
import com.petpet.c3po.parameters.ProfileParams;

public class C3PO {

  private static final Logger LOG = LoggerFactory.getLogger(C3PO.class);

  public static final String VERSION = "0.4.0-SNAPSHOT";

  private Map<String, Command> commands;

  private Map<String, Params> params;

  private static final String[] MODES = { "gather", "profile", "export", "version", "help" };

  public C3PO() {
    params = new HashMap<String, Params>();
    params.put(MODES[0], new GatherParams());
    params.put(MODES[1], new ProfileParams());
    params.put(MODES[2], new ExportParams());
    params.put(MODES[3], new Params() {
    });
    params.put(MODES[4], new Params() {
    });

    commands = new HashMap<String, Command>();
    commands.put(MODES[0], new GatherCommand());
    commands.put(MODES[1], new ProfileCommand());
    commands.put(MODES[2], new ExportCommand());
    commands.put(MODES[3], new VersionCommand());
    commands.put(MODES[4], new HelpCommand(params));
  }

  private void compute(String mode, String[] args) {

    if (!Arrays.asList(MODES).contains(mode)) {
      System.err.println("Unknown mode: " + mode);
      new HelpCommand(params).execute();
      System.exit(1);
    }

    Params params = this.params.get(mode);
    JCommander jc = new JCommander(params);
    jc.setProgramName("c3po " + mode);
    try {
      jc.parse(Arrays.copyOfRange(args, 1, args.length));
      Command command = this.commands.get(mode);
      if (command == null) {
        throw new ParameterException("Unknown mode '" + mode + "'.");
      }
      command.setDelegateParams(params);
      command.execute();
      long time = command.getTime();

      if (time != -1) {
        System.out.println("Success. Execution Time: " + time + "ms");
      }
    } catch (ParameterException e) {
      LOG.warn("{}", e.getMessage());
      System.err.println(e.getMessage());
      jc.usage();
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    if (args.length == 0) {
      System.err.println("Please use one of the following");
      System.exit(1);
    }

    C3PO c3po = new C3PO();
    c3po.compute(args[0], args);
  }

}
