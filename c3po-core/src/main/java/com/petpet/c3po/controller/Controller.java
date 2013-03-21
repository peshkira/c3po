package com.petpet.c3po.controller;

import com.petpet.c3po.adaptor.fits.FITSAdaptor;
import com.petpet.c3po.adaptor.rules.EmptyValueProcessingRule;
import com.petpet.c3po.adaptor.rules.HtmlInfoProcessingRule;
import com.petpet.c3po.adaptor.rules.ProcessingRule;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.ActionLog;
import com.petpet.c3po.gatherer.FileSystemGatherer;
import com.petpet.c3po.utils.ActionLogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//TODO generalize the gatherer with the interface.
public class Controller {

  private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
  private PersistenceLayer persistence;
  private ExecutorService pool;
  private FileSystemGatherer gatherer;
  private int counter = 0;
  private long TotalSize=0;
  public Controller(PersistenceLayer pLayer) {
    this.persistence = pLayer;
  }
  
private void processArchivesBin( Map<String, Object> config, File[] archiveFiles, int startPosition, int endPosition)
{
    String path = (String) config.get(Constants.CNF_COLLECTION_LOCATION);
    File tmpDir = gatherer.createDirectory(archiveFiles[startPosition].getParent()+"/tmp");
    for (int j=startPosition; j < endPosition; j++)
    {
        gatherer.Extract(archiveFiles[j], tmpDir.getPath());
    }
    config.put(Constants.CNF_COLLECTION_LOCATION, tmpDir.getPath());
    {
        gatherer = new FileSystemGatherer(config);
        int threads = (Integer) config.get(Constants.CNF_THREAD_COUNT);
        Map<String, Object> adaptorcnf = this.getAdaptorConfig(config);
        String tmpSizeinGB = new DecimalFormat("#####.#####").format(gatherer.getSize()/1024.0/1024.0/1024.0); 
        TotalSize +=gatherer.getSize();
        LOGGER.info("{} files of size "+ tmpSizeinGB +" GB to be processed for collection {}", gatherer.getCount(), config.get(Constants.CNF_COLLECTION_NAME));
        this.startJobs(threads, adaptorcnf);
    }
    gatherer.deleteDirectory(tmpDir.getPath());
    config.put(Constants.CNF_COLLECTION_LOCATION, path);
     

}

  public void collect(Map<String, Object> config) {
    boolean isToExtract = (Boolean) config.get(Constants.CNF_EXTRACT);
      
    if (!isToExtract) {
        this.gatherer = new FileSystemGatherer(config);

        int threads = (Integer) config.get(Constants.CNF_THREAD_COUNT);
        Map<String, Object> adaptorcnf = this.getAdaptorConfig(config);

        LOGGER.info("{} files to be processed for collection {}", gatherer.getCount(),
            config.get(Constants.CNF_COLLECTION_NAME));
     //IMPLEMENT HERE PARTIAL PROCESSING OF ZIP-FILES
        this.startJobs(threads, adaptorcnf);
    } else {
        gatherer = new FileSystemGatherer(isToExtract);
        String path = (String) config.get(Constants.CNF_COLLECTION_LOCATION);
        File dir = new File(path);
        
        File[] ArchiveFiles=gatherer.GetListOfFiles(dir, gatherer.archivefilter);
        int archivesPerBin=50;
        LOGGER.info("{} archives to be processed", ArchiveFiles.length);
        try{
            int startPosition=0;
            int endPosition=0;
            
            while (endPosition <= ArchiveFiles.length && startPosition<=endPosition) {                
               endPosition= (startPosition+archivesPerBin)<ArchiveFiles.length?(startPosition+archivesPerBin):ArchiveFiles.length;
               processArchivesBin(config, ArchiveFiles, startPosition, endPosition);
               startPosition += archivesPerBin;
               
            }
            String tmpSizeinGB = new DecimalFormat("###########.#####").format(TotalSize/1024.0/1024.0/1024.0); 
            LOGGER.info("TOTAL: {} files of size "+ tmpSizeinGB +" GB processed for collection {}", counter, config.get(Constants.CNF_COLLECTION_NAME));
        } catch (Exception ignored){ }
    }
  }

  private Map<String, Object> getAdaptorConfig(Map<String, Object> config) {
    final Map<String, Object> adaptorcnf = new HashMap<String, Object>();
    for (String key : config.keySet()) {
      if (key.startsWith("adaptor.")) {
        adaptorcnf.put(key, config.get(key));
      }
    }

    adaptorcnf.put(Constants.CNF_COLLECTION_ID, config.get(Constants.CNF_COLLECTION_NAME));
    return adaptorcnf;
  }

  private void startJobs(int threads, Map<String, Object> adaptorcnf) {
    this.pool = Executors.newFixedThreadPool(threads);
    List<ProcessingRule> rules = this.getRules();

    for (int i = 0; i < threads; i++) {
      final FITSAdaptor f = new FITSAdaptor();
      f.setController(this);
      f.setRules(rules);
      f.configure(adaptorcnf);

      this.pool.submit(f);
    }

    this.pool.shutdown();

    try {
      // What happens if the time out occurrs first?
      boolean terminated = this.pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

      if (terminated) {
        LOGGER.info("Gathering process finished successfully");
        String collection = (String) adaptorcnf.get(Constants.CNF_COLLECTION_ID);
        ActionLog log = new ActionLog(collection, ActionLog.UPDATED_ACTION);
        new ActionLogHelper(this.persistence).recordAction(log);
        
      } else {
        LOGGER.error("Time out occurred, gathering process was terminated");
      }

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public PersistenceLayer getPersistence() {
    return this.persistence;
  }

  public synchronized InputStream getNext() {
    List<InputStream> next = this.gatherer.getNext(1);
    InputStream result = null;

    if (!next.isEmpty()) {
      result = next.get(0);
    }

    this.counter++;

    if (counter % 1000 == 0) {
      LOGGER.info("Finished processing {} files", counter);
      
    }

    return result;
  }

  // TODO this should be generated via some user input.
  private List<ProcessingRule> getRules() {
    List<ProcessingRule> rules = new ArrayList<ProcessingRule>();
    rules.add(new HtmlInfoProcessingRule());
    rules.add(new EmptyValueProcessingRule());
    return rules;
  }

}
