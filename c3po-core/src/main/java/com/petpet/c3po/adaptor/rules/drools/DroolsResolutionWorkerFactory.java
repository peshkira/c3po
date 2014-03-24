package com.petpet.c3po.adaptor.rules.drools;

import com.petpet.c3po.utils.C3POFileUtils;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import sun.misc.Launcher;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DroolsResolutionWorkerFactory {

    /**
     * The {@link KnowledgeBase} holding all compiled rules.
     */
    private KnowledgeBase kbase;
    private boolean rulesAvailable;

    public DroolsResolutionWorkerFactory(String pathToRules) {

        initKnowledgeBase(pathToRules);
    }


    public DroolsResolutionWorker createWorker() {
        DroolsResolutionWorker worker = new DroolsResolutionWorker(this.kbase);
        return worker;
    }

    private void initKnowledgeBase(String pathToRules) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        List<File> sources=readRules(pathToRules);
        for (File filename : sources) {
            kbuilder.add(ResourceFactory.newFileResource(filename), ResourceType.DRL);
            System.out.println("Adding rule: " + filename.getPath());
        }

        if (kbuilder.hasErrors()) {
      /* TODO: proper handling/logging of errors! */
            System.err.println(kbuilder.getErrors().toString());
        }

        this.kbase = KnowledgeBaseFactory.newKnowledgeBase();
        this.kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        if (sources.isEmpty()){
            System.out.println("No conflict reduction rules found in 'rules' folder. The process will continue.");
            rulesAvailable=false;
            return;
        }
        rulesAvailable=true;
        RuleActivationListener.getInstance().initialize(
                this.kbase.getKnowledgePackages());
    }

    private List<File> readRules(String pathToRules){
        List<File> result= new ArrayList<File>();
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        File rulesDir=new File(pathToRules);
        if (rulesDir.exists())
        {
            if (rulesDir.isDirectory()) {
                List<File> tmp_files= C3POFileUtils.traverseDirectory(rulesDir);
                for (File file : tmp_files) {
                    if (file.isFile() && file.getName().endsWith(".drl")) {
                        result.add(file);
                    }
                }
            } else if (rulesDir.isFile() && rulesDir.getName().endsWith(".drl")) {
                result.add(rulesDir);
            }
        }
        else if (!jarFile.isFile())
        {
            final URL url = Launcher.class.getResource("/" + pathToRules);
            if (url != null) {
                try {
                    final File directory = new File(url.toURI());
                    result.addAll(C3POFileUtils.traverseDirectory(directory));
                } catch (URISyntaxException ex) {
                    // never happens
                }
            }
        }
        return result;
    }
    public boolean isRulesAvailable()
    {
        return rulesAvailable;
    }

}