package com.petpet.c3po.adaptor.rules.drools;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;

public class DroolsResolutionWorkerFactory {

  /**
   * The {@link KnowledgeBase} holding all compiled rules.
   */
  private KnowledgeBase kbase;

  public DroolsResolutionWorkerFactory() {
  }

  public DroolsResolutionWorker createWorker() {
    DroolsResolutionWorker worker = new DroolsResolutionWorker(this.kbase);
    return worker;
  }

  public void setSource(File source) {
    List<File> filenames = new LinkedList<File>();

    if (source.isDirectory()) {
      for (File file : source.listFiles()) {
        if (file.isFile() && file.getName().endsWith(".drl")) {
          filenames.add(file);
        }
      }
    } else if (source.isFile() && source.getName().endsWith(".drl")) {
      filenames.add(source);
    }

    this.initKnowledgeBase(filenames);
  }

    public void setSource(List<File> sources) {
        this.initKnowledgeBase(sources);
    }

  private void initKnowledgeBase(List<File> sources) {
    KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

    // load basic ruleset
    kbuilder.add(ResourceFactory.newClassPathResource(
        "/rules/conflictResolutionBasicRules.drl", this.getClass()),
        ResourceType.DRL);

    // load additional rules
    for (File filename : sources) {
      kbuilder.add(ResourceFactory.newFileResource(filename), ResourceType.DRL);
    }

    if (kbuilder.hasErrors()) {
      /* TODO: proper handling/logging of errors! */
      System.err.println(kbuilder.getErrors().toString());
    }

    this.kbase = KnowledgeBaseFactory.newKnowledgeBase();
    this.kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

    RuleActivationListener.getInstance().initialize(
        this.kbase.getKnowledgePackages());
  }

}