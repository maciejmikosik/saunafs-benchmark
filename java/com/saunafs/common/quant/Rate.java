package com.saunafs.common.quant;

import java.time.Duration;

public class Rate implements Comparable<Rate> {
  public static final Rate ZERO = new Rate(0);

  private final double bytesPerSecond;

  private Rate(double bytesPerSecond) {
    this.bytesPerSecond = bytesPerSecond;
  }

  public static Rate rate(Size size, Duration duration) {
    var bytes = size.inBytes();
    var seconds = duration.toNanos() * 1e-9;
    return new Rate(bytes / seconds);
  }

  public static Rate bytesPerSecond(double bytesPerSecond) {
    return new Rate(bytesPerSecond);
  }

  public double inBytesPerSecond() {
    return bytesPerSecond;
  }

  public double inMebibytesPerSecond() {
    return bytesPerSecond / (1 << 20);
  }

  public int compareTo(Rate that) {
    return Double.compare(bytesPerSecond, that.bytesPerSecond);
  }

  public double divideBy(Rate divisor) {
    return bytesPerSecond / divisor.bytesPerSecond;
  }

  public boolean equals(Object that) {
    return that instanceof Rate rate
        && bytesPerSecond == rate.bytesPerSecond;
  }

  public int hashCode() {
    return Double.hashCode(bytesPerSecond);
  }
}
