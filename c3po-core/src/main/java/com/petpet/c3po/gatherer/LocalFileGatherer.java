package com.petpet.c3po.gatherer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.gatherer.MetaDataGatherer;
import com.petpet.c3po.api.model.helper.MetadataStream;
import com.petpet.c3po.common.Constants;

//TODO try to lock/synchronize on the queue instead of this!
public class LocalFileGatherer implements MetaDataGatherer {

  private static final Logger LOG = LoggerFactory.getLogger(LocalFileGatherer.class);

  private Map<String, String> config;

  private final Queue<String> queue;

  private long sum;

  private boolean ready;

  private Object lock;

  public LocalFileGatherer() {
    this.queue = new LinkedList<String>();
    this.ready = false;
  }
  
  public LocalFileGatherer(Map<String, String> config) {
    this();
    this.config = config;
  }
  
  public LocalFileGatherer(Object lock) {
    this();
    this.lock = lock;
  }

  @Override
  public synchronized void run() {
    String path = this.config.get(Constants.OPT_COLLECTION_LOCATION);
    boolean recursive = Boolean.valueOf(this.config.get(Constants.OPT_RECURSIVE));

    this.ready = false;
    this.traverseFiles(new File(path), recursive, true);
    LOG.info("{} files were gathered successfully", this.sum);
    this.ready = true;
    synchronized (lock) {
      this.lock.notifyAll();
      
    }
  }

  @Override
  public List<MetadataStream> getNext(int count) {
    List<MetadataStream> result = new ArrayList<MetadataStream>();
    if (count > 0) {

      while (count > 0) {
        MetadataStream next = this.getNext();
        if (next == null) {
          break;

        } else {
          result.add(next);
        }

        count--;
      }
    }
    return result;
  }

  public MetadataStream getNext() {
    //synchronized (queue) {

      MetadataStream result = null;
      String filePath = queue.poll();
      if (filePath != null) {
        try {
          result = new MetadataStream(filePath, new FileInputStream(new File(filePath)));
        } catch (FileNotFoundException e) {
          LOG.warn("File not found: {}. {}", filePath, e.getMessage());
        }
      }

      return result;

    //}
  }

  private void traverseFiles(File file, boolean recursive, boolean firstLevel) {

    if (file.isDirectory() && (recursive || firstLevel)) {
      File[] files = file.listFiles();
      for (File f : files) {
        traverseFiles(f, recursive, false);
      }
    } else {
        this.queue.add(file.getAbsolutePath());
        sum++;
        
//    synchronized (lock) {
        if ((this.sum % 1000) == 0) {
          LOG.info("traversed: {} files", this.sum);
//          this.lock.notify();
     //}
     }
    }

  }

  @Override
  public void setConfig(Map<String, String> config) {
    this.config = config;
  }

  @Override
  public long getCount() {
    throw new UnsupportedOperationException("This method is deprecated and not supported anymore.");
  }

  @Override
  public long getRemaining() {
    throw new UnsupportedOperationException("This method is deprecated and not supported anymore.");
  }

  @Override
  public boolean hasNext() {
//    synchronized (queue) {
      return !this.queue.isEmpty();
//    }
  }

  @Override
  public boolean isReady() {
    return this.ready;
  }
}
