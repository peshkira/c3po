package com.petpet.c3po.api.model.helper;

public class NumericStatistics {

  private long count;
  
  private double sum;
  
  private double min;
  
  private double max;
  
  private double average;
  
  private double standardDeviation;
  
  private double variance;
  
  public NumericStatistics() {
    
  }
  
  public NumericStatistics(long c, double s, double m, double mx, double a, double sd, double v) {
    count = c;
    sum = s;
    min = m;
    max = mx;
    average = a;
    standardDeviation = sd;
    variance = v;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }

  public double getSum() {
    return sum;
  }

  public void setSum(double sum) {
    this.sum = sum;
  }

  public double getMin() {
    return min;
  }

  public void setMin(double min) {
    this.min = min;
  }

  public double getMax() {
    return max;
  }

  public void setMax(double max) {
    this.max = max;
  }

  public double getAverage() {
    return average;
  }

  public void setAverage(double average) {
    this.average = average;
  }

  public double getStandardDeviation() {
    return standardDeviation;
  }

  public void setStandardDeviation(double standardDeviation) {
    this.standardDeviation = standardDeviation;
  }

  public double getVariance() {
    return variance;
  }

  public void setVariance(double variance) {
    this.variance = variance;
  }
}
