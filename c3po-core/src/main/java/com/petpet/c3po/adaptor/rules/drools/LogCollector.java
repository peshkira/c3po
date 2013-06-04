package com.petpet.c3po.adaptor.rules.drools;

public class LogCollector {

  private StringBuilder stringBuilder = new StringBuilder();

  public synchronized void debug(String text) {
    this.stringBuilder.append(text);
    this.stringBuilder.append("\n");
  }

  public synchronized void log(String text) {
    this.stringBuilder.append(text);
    this.stringBuilder.append("\n");
  }

  public synchronized String reset() {
    String string = this.stringBuilder.toString();
    this.stringBuilder.setLength(0);
    return string;

  }
}
