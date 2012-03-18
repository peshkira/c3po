package com.petpet.c3po.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;

public class C3POFileAppender extends FileAppender {
  private static final String LOG_PATH = "logs/";

  private static final String LOG_NAME = "c3po_";

  private static final String LOG_EXT = ".log";

  public C3POFileAppender() {
    final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    this.setFile(C3POFileAppender.LOG_PATH + LOG_NAME + formatter.format(new Date()) + LOG_EXT);
  }
}
