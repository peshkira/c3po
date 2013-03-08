package com.petpet.c3po.gatherer;

import com.petpet.c3po.api.MetaDataGatherer;
import com.petpet.c3po.common.Constants;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;



public class FileSystemGatherer implements MetaDataGatherer {

  private static final Logger LOG = LoggerFactory.getLogger(FileSystemGatherer.class);

  private Map<String, Object> config;

  private List<String> files;

  private long count;

  private long remaining;

  private int pointer;

  public FileSystemGatherer(Boolean isToExtract){
   
      archivefilter=new ArchiveFileFilter(isToExtract);
  
  }
  
  public FileSystemGatherer(Map<String, Object> config) {
    this.config = config;
    this.init();
  }

  public void setConfig(Map<String, Object> config) {
    this.config = config;

  }

  public long getCount() {
    return this.count;
  }

  @Override
  public long getRemaining() {
    return this.remaining;
  }

  public List<InputStream> getNext(int nr) {
    List<InputStream> next = new ArrayList<InputStream>();

    if (nr <= 0) {
      return next;
    }

    while (this.pointer < this.files.size() && nr > 0) {
      try {
        nr--;
        this.remaining--;
        next.add(new FileInputStream(this.files.get(pointer++)));
      } catch (FileNotFoundException e) {
        LOG.warn("File '{}' not found: {}", this.files.get(this.pointer), e.getMessage());
      }
    }

    return next;
  }
 //boolean isToExtract;
  private void init() {
    this.files = new ArrayList<String>();
    this.pointer = 0;
    this.count = -1;
    this.CollectionSizeBytes=0;
    this.remaining = -1;
    String path = (String) this.config.get(Constants.CNF_COLLECTION_LOCATION);
    boolean recursive = (Boolean) this.config.get(Constants.CNF_RECURSIVE);
    boolean extract = (Boolean) this.config.get(Constants.CNF_EXTRACT);
    if (path == null) {
      LOG.error("No path config provided");
      return;
    }

    File dir = new File(path);

    if (!dir.exists() || !dir.isDirectory()) {
      LOG.error("Directory '{}' does not exist, or is not a directory", path);
      throw new ExceptionInInitializerError("Directory does not exist, or is not a directory");
    }
    if (extract)
    {
        archivefilter = new ArchiveFileFilter(extract);
        long traverseArchiveFiles = traverseArchiveFiles(dir, archivefilter);
    }
    final XMLFileFilter filter = new XMLFileFilter(recursive);
    this.count += this.traverseFiles(dir, filter);
    this.remaining = this.count;

  }
  private long CollectionSizeBytes;
  public long getSize()
  {
      return CollectionSizeBytes;
  }
  public ArchiveFileFilter archivefilter;
  private long traverseFiles(File file, FileFilter filter) {
    long sum = 0;

    if (file.isDirectory()) {
        File[] files = file.listFiles(filter);
        for (File f : files) {
            sum += traverseFiles(f, filter);
        }
    } else {
        this.files.add(file.getAbsolutePath());
        CollectionSizeBytes += file.length();
        sum++;
        }

    return sum;
  }
  
  private void Extract(File file) throws Exception {
    //String s;
    try {

      Process  p = Runtime.getRuntime().exec("tar -xzf " + file.getPath() + " -C " + file.getParent());
     //    BufferedReader br = new BufferedReader(
     //       new InputStreamReader(p.getInputStream()));
     //   while ((s = br.readLine()) != null)
     //       System.out.println("line: " + s);
        System.out.println("Extracting: " + file.getPath() );
        p.waitFor();
       // System.out.println ("exit: " + p.exitValue());
        p.destroy();
    } catch (Exception ignored) {}
  }
  
    public void Extract(File file, String Destination) {
    //String s;
    try {
      Process  p = Runtime.getRuntime().exec("tar -xzf " + file.getPath() + " -C " + Destination);
     //    BufferedReader br = new BufferedReader(
     //       new InputStreamReader(p.getInputStream()));
     //   while ((s = br.readLine()) != null)
     //       System.out.println("line: " + s);
        System.out.println("Extracting: " + file.getPath() );
        p.waitFor();
       // System.out.println ("exit: " + p.exitValue());
        p.destroy();
    } catch (Exception ignored) {}
  }
  
  
  public File[] GetListOfFiles(File file, FileFilter filter){
    if (file.exists() && file.isDirectory())
        return file.listFiles(filter);
    LOG.error("Directory '{}' does not exist, or is not a directory", file);
      return new File[]{};
          
  }
      
  public File createDirectory(String Directory){
    File theDir = new File(Directory);
  // if the directory does not exist, create it
    if (!theDir.exists())
    {
        LOG.info("Creating a directory: " + Directory);
        try {
            FileUtils.deleteDirectory(theDir);
            FileUtils.forceMkdir(theDir);
        } catch (IOException ignored) {    }
    }
    return theDir;
  }
  
    public void deleteDirectory(String Directory){
    File theDir = new File(Directory);
      try {
           LOG.info("Deleting a directory: " + Directory);
          FileUtils.deleteDirectory(theDir);
      } catch (IOException ignored) {  }
  }
  
  
  long traverseArchiveFiles(File file, FileFilter filter) {
    long sum = 0;
    if (file.isDirectory()) {

       File[] files = file.listFiles(filter);
       for (File f : files) {
           if (isArchive(f)) {
               try {
                   Extract(f);
               } catch (Exception ex) {
                   java.util.logging.Logger.getLogger(FileSystemGatherer.class.getName()).log(Level.SEVERE, null, ex);
               }
               }
          }
       files = file.listFiles(filter);
        
        
      
      for (File f : files) {
        sum += traverseArchiveFiles(f, filter);
      }
    } 

    return sum;
  }
  
 public static boolean isArchive(File pathname){
      return  (pathname.getName().endsWith(".zip") ||
          pathname.getName().endsWith(".bzip2") ||
          pathname.getName().endsWith(".gzip") ||
          pathname.getName().endsWith(".jar") ||
          pathname.getName().endsWith(".tar") ||
          pathname.getName().endsWith(".tar.gz") ||
          pathname.getName().endsWith(".tgz") ) ;
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
  
  
  public class ArchiveFileFilter implements FileFilter 
  {
      private boolean isToExtract;
     public ArchiveFileFilter(boolean isToExtract) {
      this.isToExtract = isToExtract;
    }
    public String removeExtension( String in )
    {
    int p = in.lastIndexOf(".");
    if ( p < 0 )
        return in;

    int d = in.lastIndexOf( File.separator );

    if ( d < 0 && p == 0 )
        return in;

    if ( d >= 0 && d > p )
        return in;

    return in.substring( 0, p );
    }

    @Override
    public boolean accept(File pathname) {
      boolean accept = false;
      if ( isToExtract && isArchive(pathname))
      {
          String name = removeExtension(pathname.getPath());
           if ( !(new File(name).isDirectory()))
            accept = true;
      }
      return accept;
    }
     
  }
}