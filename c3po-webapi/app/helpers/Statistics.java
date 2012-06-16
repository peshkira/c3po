package helpers;

public class Statistics {
  
  private int count;
  
  private long size;
  
  private double avg;
  
  private long min;
  
  private long max;
  
  private double sd;
  
  private double var;

  public String getCount() {
    return count + " objects";
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getSize() {
    return (size / 1024 / 1024) + "MB";
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getAvg() {
    return (avg / 1024 / 1024) + "MB";
  }

  public void setAvg(double avg) {
    this.avg = avg;
  }

  public String getMin() {
    return min + "B";
  }

  public void setMin(long min) {
    this.min = min;
  }

  public String getMax() {
    return (max / 1024 / 1024) + "MB";
  }

  public void setMax(long max) {
    this.max = max;
  }

  public String getSd() {
    return sd + "B";
  }

  public void setSd(double sd) {
    this.sd = sd;
  }

  public String getVar() {
    return var + "B";
  }

  public void setVar(double var) {
    this.var = var;
  }

}
