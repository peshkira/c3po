package com.petpet.c3po.api.model.helper;

import java.io.InputStream;

public class DigitalObjectStream {

  private String fileName;

  private InputStream data;
  
  
  public DigitalObjectStream(String fileName, InputStream data) {
    this.fileName = fileName;
    this.data = data;
  }
  
  public String getFileName() {
    return this.fileName;
  }
  
  public InputStream getData() {
    return this.data;
  }
}
