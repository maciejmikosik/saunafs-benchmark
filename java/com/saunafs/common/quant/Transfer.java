package com.saunafs.common.quant;

import java.time.Duration;

public class Transfer implements Comparable<Transfer> {
  public static final Transfer ZERO = new Transfer(0);

  private final double bytesPerSecond;

  private Transfer(double bytesPerSecond) {
    this.bytesPerSecond = bytesPerSecond;
  }

  public static Transfer transfer(Size size, Duration duration) {
    var bytes = size.inBytes();
    var seconds = duration.toNanos() * 1e-9;
    return new Transfer(bytes / seconds);
  }

  public static Transfer bytesPerSecond(double bytesPerSecond) {
    return new Transfer(bytesPerSecond);
  }

  public double inBytesPerSecond() {
    return bytesPerSecond;
  }

  public double inMebibytesPerSecond() {
    return bytesPerSecond / (1 << 20);
  }

  public int compareTo(Transfer that) {
    return Double.compare(bytesPerSecond, that.bytesPerSecond);
  }

  public double divideBy(Transfer divisor) {
    return bytesPerSecond / divisor.bytesPerSecond;
  }

  public boolean equals(Object that) {
    return that instanceof Transfer transfer
        && bytesPerSecond == transfer.bytesPerSecond;
  }

  public int hashCode() {
    return Double.hashCode(bytesPerSecond);
  }
}
