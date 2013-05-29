package com.petpet.c3po.adaptor.rules;

import java.util.Arrays;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;

import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.datamodel.Element;

public class DroolsConflictResolutionProcessingRule implements PostProcessingRule {

  public static final int PRIORITY = 500;
  private static final String CACHE = "cache";
  private final Cache cache;
  private StatelessKnowledgeSession session;

  public DroolsConflictResolutionProcessingRule(Cache cache) {
    this.cache = cache;

    // read in the source
    String filename = "/rules/conflictResolution.drl";
    List<String> filenames = Arrays.asList(filename);

    this.initSession(filenames);

  }

  private void initSession(List<String> filenames) {
    KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

    for (String filename : filenames) {
      kbuilder.add(ResourceFactory.newClassPathResource(filename, this.getClass()), ResourceType.DRL);
    }

    if (kbuilder.hasErrors()) {
      System.err.println(kbuilder.getErrors().toString());
    }

    KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
    kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

    this.session = kbase.newStatelessKnowledgeSession();

    // this.session.addEventListener(new DebugWorkingMemoryEventListener());

    this.session.setGlobal(CACHE, this.cache);
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public Element process(Element e) {

    // TODO: this is only to debug the output - remove it when done!
    synchronized (System.out) {
      this.session.execute(e);
    }

    return e;
  }

}
