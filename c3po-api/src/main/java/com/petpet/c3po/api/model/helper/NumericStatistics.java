package com.petpet.c3po.api.model.helper;

/**
 * A simple object that encapsulates the results of some simple statistical
 * calculations over the values of a numeric property.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class NumericStatistics {

  /**
   * The count of objects that have this property.
   */
  private long count;

  /**
   * The sum of all values for the property that was calculated.
   */
  private double sum;

  /**
   * The smallest value.
   */
  private double min;

  /**
   * The largest value.
   */
  private double max;

  /**
   * The average of all values.
   */
  private double average;

  /**
   * The standard deviation of all values.
   */
  private double standardDeviation;

  /**
   * The variance of all values.
   */
  private double variance;

  /**
   * A default empty constructor.
   */
  public NumericStatistics() {

  }

  /**
   * Sets the passed values correspondigly.
   * 
   * @param c
   *          the count
   * @param s
   *          the sum
   * @param m
   *          the smallest value
   * @param mx
   *          the largest value
   * @param a
   *          the average
   * @param sd
   *          the standard deviation.
   * @param v
   *          the variance.
   */
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
