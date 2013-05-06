package com.petpet.c3po.command;

import java.io.File;
import java.util.UUID;

import org.apache.commons.cli.Option;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.analysis.ProfileGenerator;
import com.petpet.c3po.analysis.RepresentativeAlgorithmFactory;
import com.petpet.c3po.analysis.RepresentativeGenerator;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.utils.Configurator;

public class ProfileCommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(ProfileCommand.class);

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
    final String alg = configurator.getStringProperty("c3po.samples.algorithm");
    final RepresentativeGenerator samplesGen = new RepresentativeAlgorithmFactory().getAlgorithm(alg);
    final ProfileGenerator profileGen = new ProfileGenerator(this.pLayer, samplesGen);

    final String name = this.getCollectionName();
    final boolean include = this.getIncludeElements();
    final Filter f = new Filter(new FilterCondition("collection", name));
    
    final Document profile = profileGen.generateProfile(f, include);

    profileGen.write(profile, this.getOutputFile(name));

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
  
  private boolean getIncludeElements() {
    for (Option o : this.options) {
      if (o.getLongOpt().equals(CommandConstants.PROFILE_INCLUDE_ELEMENT_IDENTIFIERS)) {
        return true;
      }
    }
    
    return false;
  }

  @Override
  public long getTime() {
    return this.time;
  }

}
