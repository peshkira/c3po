package com.petpet.c3po.gatherer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.MetaDataGatherer;
import com.petpet.c3po.datamodel.C3POConfig;

public class FileSystemGatherer implements MetaDataGatherer {

  private static final Logger LOG = LoggerFactory.getLogger(FileSystemGatherer.class);

  private Map<String, String> config;

  private List<String> files;

  private long count;

  private int pointer;

  public FileSystemGatherer(Map<String, String> config) {
    this.config = config;
    this.init();
  }

  @Override
  public void setConfig(Map<String, String> config) {
    this.config = config;

  }

  @Override
  public long getCount() {
    return this.count;
  }

  private long count(File dir, FileFilter filter) {
    long sum = 0;

    if (dir.isDirectory()) {
      File[] files = dir.listFiles(filter);
      for (File f : files) {
        sum += count(f, filter);
      }
    } else {
      return ++sum;
    }

    return sum;
  }

  @Override
  public List<InputStream> getNext(int count) {
    List<InputStream> next = new ArrayList<InputStream>();

    if (count <= 0) {
      return next;
    }

    while (pointer < files.size() && count > 0) {
      try {
        next.add(new FileInputStream(this.files.get(pointer++)));
        count--;
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }

    return next;
  }

  @Override
  public List<InputStream> getAll() {
    List<InputStream> all = new ArrayList<InputStream>();
    for (String path : this.files) {
      try {
        all.add(new FileInputStream(path));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
    return all;
  }

  private void init() {
    this.files = new ArrayList<String>();
    this.pointer = 0;
    String path = this.config.get(C3POConfig.LOCATION);
    boolean recursive = Boolean.valueOf(this.config.get(C3POConfig.RECURSIVE));

    if (path == null) {
      LOG.error("No path config provided");
      return;
    }

    File dir = new File(path);

    if (!dir.exists() || !dir.isDirectory()) {
      LOG.error("Directory '{}' does not exist, or is not a directory", path);
      return;
    }

    final XMLFileFilter filter = new XMLFileFilter(recursive);
    this.traverseFiles(dir, filter);
    this.count = this.count(dir, filter);

  }

  private void traverseFiles(File file, FileFilter filter) {
    if (file.isDirectory()) {
      File[] files = file.listFiles(filter);
      for (File f : files) {
        traverseFiles(f, filter);
      }
    } else {
      this.files.add(file.getAbsolutePath());
    }
  }

  private class XMLFileFilter implements FileFilter {

    private boolean recursive;

    public XMLFileFilter(boolean recursive) {
      this.recursive = recursive;
    }

    @Override
    public boolean accept(File pathname) {
      boolean accept = false;

      if ((pathname.isDirectory() && this.recursive) || pathname.getName().endsWith(".xml"))
        accept = true;

      return accept;
    }

  }

}
