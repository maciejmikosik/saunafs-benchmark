package com.saunafs.common.quant;

import java.time.Duration;

public class Transfer {
  private final double bytesPerSecond;

  private Transfer(double bytesPerSecond) {
    this.bytesPerSecond = bytesPerSecond;
  }

  public static Transfer transfer(Size size, Duration duration) {
    var bytes = size.inBytes();
    var seconds = duration.toNanos() * 1e-9;
    return new Transfer(bytes / seconds);
  }

  public double inMebibytesPerSecond() {
    return bytesPerSecond / (1 << 20);
  }
}
