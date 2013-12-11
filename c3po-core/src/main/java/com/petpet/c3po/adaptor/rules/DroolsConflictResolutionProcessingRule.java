package com.petpet.c3po.adaptor.rules;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.petpet.c3po.adaptor.rules.drools.DroolsResolutionStatisticsPrinter;
import com.petpet.c3po.adaptor.rules.drools.DroolsResolutionWorker;
import com.petpet.c3po.adaptor.rules.drools.DroolsResolutionWorkerFactory;
import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;
import sun.misc.Launcher;

public class DroolsConflictResolutionProcessingRule implements
    PostProcessingRule {

  public static final int PRIORITY = 500;

  /**
   * Hold a {@link DroolsResolutionWorker} for each thread to allow
   * multithreading without side-effects between threads.
   */
  private Map<Thread, DroolsResolutionWorker> workers;

  private DroolsResolutionWorkerFactory factory;

  public DroolsConflictResolutionProcessingRule() {

    this.factory = new DroolsResolutionWorkerFactory();
    this.workers = new ConcurrentHashMap<Thread, DroolsResolutionWorker>();
     List<File> fileList= new ArrayList<File>();
    // read in the source

      // TODO: make this configurable/extendable by the user via commandline
      // parameters
      //URL url = this.getClass().getResource("/rules/additionals");

      final String path = "rules/additionals";
      final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

      if(jarFile.isFile()) {  // Run with JAR file
          JarFile jar = null;
          try {
              jar = new JarFile(jarFile);
              final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
              while(entries.hasMoreElements()) {
                  final String name = entries.nextElement().getName();
                  if (name.startsWith(path + "/")) { //filter according to the path
                      System.out.println(name);
                  }
              }
              jar.close();
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
      else
      {
          final URL url = Launcher.class.getResource("/" + path);
          if (url != null) {
              try {
                  final File directory = new File(url.toURI());
                  for (File file : directory.listFiles()) {
                      System.out.println(file);
                  }
              } catch (URISyntaxException ex) {
                  // never happens
              }
          }

      }
      //InputStream is = ClassLoader.getSystemResourceAsStream("/rules/additionals");
      //String s= String.valueOf(url);
      //this.factory.setSource(new File(String.valueOf(url)));
      this.factory.setSource(fileList);
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public void onCommandFinished() {
    // TODO: make the execution of these methods configurable
    // RuleActivationListener.getInstance().printStatistics(System.out, false);
    DroolsResolutionStatisticsPrinter.printRuleActivation(System.out, false);

    DroolsResolutionStatisticsPrinter.printConflictsAccumulated(System.out,
        false);
    // DroolsResolutionStatisticsPrinter.printConflicts(System.out, false);
  }

  @Override
  public Element process(Element e) {

    DroolsResolutionWorker worker = this.workers.get(Thread.currentThread());
    if (worker == null) {
      worker = this.factory.createWorker();
      this.workers.put(Thread.currentThread(), worker);
    }

    worker.process(e);

    return e;
  }
}
