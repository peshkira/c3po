package com.petpet.c3po;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.command.AnonymizeCommand;
import com.petpet.c3po.command.CSVExportCommand;
import com.petpet.c3po.command.CommandConstants;
import com.petpet.c3po.command.GatherCommand;
import com.petpet.c3po.command.HelpCommand;
import com.petpet.c3po.command.ProfileCommand;
import com.petpet.c3po.command.VersionCommand;
import com.petpet.c3po.command.WrongArgumentCommand;

public class C3PO {

  private static final Logger LOG = LoggerFactory.getLogger(C3PO.class);

  public static final String VERSION = "0.1.1";

  private Options getOptions() {

    final Option gather = OptionBuilder.hasArgs(1).withArgName(CommandConstants.GATHER_DIR_ARGUMENT)
        .withDescription(CommandConstants.GATHER_DESCRIPTION).isRequired(true)
        .withLongOpt(CommandConstants.GATHER_OPTION).create("g");

    final Option profile = OptionBuilder.hasOptionalArgs(1).withArgName(CommandConstants.PROFILE_FILEPATH_ARGUMENT)
        .withDescription(CommandConstants.PROFILE_DESCRIPTION).isRequired(true)
        .withLongOpt(CommandConstants.PROFILE_OPTION).create("p");

    final Option export = OptionBuilder.hasOptionalArgs(2).withArgName(CommandConstants.EXPORT_OUTPUT_PATH)
        .withDescription(CommandConstants.EXPORT_DESCRIPTION).isRequired(true)
        // .withArgName(CommandConstants.EXPORT_MIME).withDescription(CommandConstants.EXPORT_MIME_DESCRIPTION)
        .withLongOpt(CommandConstants.EXPORT_OPTION).create("e");

//    final Option anonymize = OptionBuilder.withDescription(CommandConstants.ANONYMIZE_DESCRIPTION).isRequired(true)
//        .withLongOpt(CommandConstants.ANONYMIZE_OPTION).create("a");

    final Option collection = OptionBuilder.hasArgs(1).withArgName(CommandConstants.COLLECTION_ID_ARGUMENT)
        .withDescription(CommandConstants.COLLECTION_DESCRIPTION).isRequired(true)
        .withLongOpt(CommandConstants.COLLECTION_OPTION).create("c");

    final Option recursive = new Option("r", CommandConstants.RECURSIVE_OPTION, false,
        CommandConstants.RECURSIVE_DESCRIPTION);

    final Option includeelements = new Option("ie", CommandConstants.PROFILE_INCLUDE_ELEMENT_IDENTIFIERS, false,
        CommandConstants.PROFILE_INCLUDE_ELEMENTS_DESCRIPTION);

    final Option help = new Option("h", CommandConstants.HELP_OPTION, false, CommandConstants.HELP_DESCRIPTION);

    final Option version = new Option("v", CommandConstants.VERSION_OPTION, false, CommandConstants.VERSION_DESCRIPTION);

    final OptionGroup exclusive = new OptionGroup();
    exclusive.setRequired(false);
    exclusive.addOption(gather);
    exclusive.addOption(profile);
    exclusive.addOption(export);
//    exclusive.addOption(anonymize);

    final OptionGroup exclusive2 = new OptionGroup();
    exclusive2.setRequired(true);
    exclusive2.addOption(help);
    exclusive2.addOption(version);
    exclusive2.addOption(collection);
    
    final OptionGroup exclusive3 = new OptionGroup();
    exclusive3.addOption(recursive);
    exclusive3.addOption(includeelements);

    final Options options = new Options();
    options.addOptionGroup(exclusive);
    options.addOptionGroup(exclusive2);
    options.addOptionGroup(exclusive3);

    return options;
  }

  private void compute(Options o, String[] args) {
    final CommandLineParser parser = new BasicParser();

    try {
      final CommandLine line = parser.parse(o, args);

      if (line.hasOption(CommandConstants.HELP_OPTION)) {

        new HelpCommand(o).execute();

      } else if (line.hasOption(CommandConstants.VERSION_OPTION)) {
        
        new VersionCommand().execute();

      } else if (line.hasOption(CommandConstants.GATHER_OPTION)) {

        final GatherCommand cmd = new GatherCommand(line.getOptions());
        cmd.execute();
        LOG.info("Execution time: {}ms", cmd.getTime());
        
      } else if (line.hasOption(CommandConstants.PROFILE_OPTION)) {

        final ProfileCommand cmd = new ProfileCommand(line.getOptions());
        cmd.execute();
        LOG.info("Execution time: {}ms", cmd.getTime());
        
      } else if (line.hasOption(CommandConstants.ANONYMIZE_OPTION)) {

        final AnonymizeCommand cmd = new AnonymizeCommand(line.getOptions());
        cmd.execute();
        LOG.info("Execution time: {}ms", cmd.getTime());
        
      } else if (line.hasOption(CommandConstants.EXPORT_OPTION)) {
        
        final CSVExportCommand cmd = new CSVExportCommand(line.getOptions());
        cmd.execute();
        LOG.info("Execution time: {}ms", cmd.getTime());

      }

    } catch (final ParseException e) {
      new WrongArgumentCommand(e.getMessage(), o).execute();
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    final C3PO c3po = new C3PO();
    final Options options = c3po.getOptions();
    c3po.compute(options, args);
  }

}
