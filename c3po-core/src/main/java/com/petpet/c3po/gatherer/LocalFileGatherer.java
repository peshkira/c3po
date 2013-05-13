package com.petpet.c3po.gatherer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.gatherer.MetaDataGatherer;
import com.petpet.c3po.api.model.helper.MetadataStream;
import com.petpet.c3po.common.Constants;

public class LocalFileGatherer implements MetaDataGatherer {

  private static final Logger LOG = LoggerFactory.getLogger(LocalFileGatherer.class);

  private Map<String, String> config;

  private final Queue<MetadataStream> queue;

  private long sum;

  private boolean ready;

  private Object lock;

  public LocalFileGatherer() {
    this.queue = new LinkedList<MetadataStream>();
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
    System.out.println(this.sum + " files were gathered successfully");
    LOG.info("{} files were gathered successfully", this.sum);
    this.ready = true;
    synchronized (lock) {
      this.lock.notifyAll();

    }
  }

  @Deprecated
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
    synchronized (lock) {
      return queue.poll();
    }
  }

  private String readStream(String name, InputStream data) {
    String result = null;
    try {
      result = IOUtils.toString(data);
    } catch (IOException e) {
      LOG.warn("An error occurred, while reading the stream for {}: {}", name, e.getMessage());
    } finally {
      IOUtils.closeQuietly(data);
    }
    return result;
  }

  private void traverseFiles(File file, boolean recursive, boolean firstLevel) {

    if (file.isDirectory() && (recursive || firstLevel)) {

      File[] files = file.listFiles();
      for (File f : files) {
        traverseFiles(f, recursive, false);
      }
    } else {
      String filePath = file.getAbsolutePath();
      try {
        String data = this.readStream(filePath, new BufferedInputStream(new FileInputStream(new File(filePath)), 8192));
        MetadataStream result = new MetadataStream(filePath, data);
        this.queue.add(result);
        sum++;
      } catch (FileNotFoundException e) {
        LOG.warn("File not found: {}. {}", filePath, e.getMessage());
      }

      if ((this.sum % 1000) == 0) {
        LOG.info("traversed: {} files", this.sum);
        synchronized (lock) {
          this.lock.notify();

        }
      }

      if (this.queue.size() > 10000 && this.sum % 1000 == 0) {
        synchronized (lock) {
          this.lock.notifyAll();
        }
      }

      if (this.sum % 10000 == 0) {
        System.out.println(this.sum + " files were processed");
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
    return !this.queue.isEmpty();
  }

  @Override
  public boolean isReady() {
    return this.ready;
  }
}
