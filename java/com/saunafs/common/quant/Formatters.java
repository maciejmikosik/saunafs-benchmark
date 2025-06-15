package com.saunafs.common.quant;

import java.time.Duration;

public class Formatters {
  public static Formatter<Size> sizeInBytes() {
    return new Formatter<Size>() {
      public String format(Size size) {
        return "" + size.inBytes();
      }

      public String unit() {
        return "B";
      }
    };
  }

  public static Formatter<Duration> durationInSeconds() {
    return new Formatter<Duration>() {
      public String format(Duration duration) {
        return "%.9f".formatted(duration.toNanos() * 1e-9);
      }

      public String unit() {
        return "s";
      }
    };
  }
}
