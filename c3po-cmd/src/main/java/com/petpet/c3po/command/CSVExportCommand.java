package com.petpet.c3po.command;

import java.io.File;

import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.analysis.CSVGenerator;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.utils.Configurator;

public class CSVExportCommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(CSVExportCommand.class);

  private Option[] options;

  private long time = -1L;

  private CSVGenerator generator;

  public CSVExportCommand(Option[] options) {
    this.options = options;

  }

  @Override
  public void execute() {
    long start = System.currentTimeMillis();
    LOG.info("Starting csv export of all data");

    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();

    final PersistenceLayer pLayer = configurator.getPersistence();
    this.generator = new CSVGenerator(pLayer);

    this.generator.exportAll(this.getCollectionName(), this.getOutputFile("matrix"));

    long end = System.currentTimeMillis();
    this.time = end - start;
  }

  @Override
  public long getTime() {
    return this.time;
  }

  private String getCollectionName() {
    for (Option o : this.options) {
      if (o.getArgName().equals(CommandConstants.COLLECTION_ID_ARGUMENT)) {
        return o.getValue();
      }
    }

    LOG.warn("No collection identifier found, using DefaultCollection");
    return "DefaultCollection";
  }

  private String getOutputFile(String name) {
    final String extension = ".csv";
    String result = null;

    for (Option o : this.options) {
      if (o.getArgName().equals(CommandConstants.EXPORT_OUTPUT_PATH)) {
        result = o.getValue();
      }
    }

    if (result != null) {
      return result + File.separator + name + extension;
    }

    LOG.debug("No output filepath was specified, using default");
    return name + extension;
  }

}
