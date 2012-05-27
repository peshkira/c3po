package com.petpet.c3po.command;

import java.io.File;

import org.apache.commons.cli.Option;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.analysis.ProfileGenerator;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.utils.Configurator;

public class ProfileCommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(ProfileCommand.class);

  private static final String[] PROPERTIES = { "format", "format.version", "puid", "mimetype", "compressionscheme",
      "creating.application.name", "creating.os", "has.forms", "has.annotations", "has.outline", "is.protected", "is.rightsmanaged", "valid", "wellformed" };

  private Option[] options;
  private PersistenceLayer pLayer;
  private long time = -1L;

  public ProfileCommand(Option[] options) {
    this.options = options;
  }

  @Override
  public void execute() {
    final long start = System.currentTimeMillis();
    
    
    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();

    this.pLayer = configurator.getPersistence();
    final ProfileGenerator gen = new ProfileGenerator(this.pLayer);
    
    final String name = this.getCollectionName();
    final Document profile = gen.generateProfile(name);
    
    gen.write(profile, this.getOutputFile(name));
    
    final long end = System.currentTimeMillis();
    this.time = end - start;
  }

  private String getOutputFile(String name) {
    final String extension = ".xml";
    String result = null;

    for (Option o : this.options) {
      if (o.getArgName().equals(CommandConstants.PROFILE_FILEPATH_ARGUMENT)) {
        result = o.getValue();
      }
    }

    if (result != null) {
      return result + File.separator + name + extension;
    }

    LOG.debug("No output filepath was specified, using default");
    return name + ".xml";
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

  @Override
  public long getTime() {
    return this.time;
  }

}
