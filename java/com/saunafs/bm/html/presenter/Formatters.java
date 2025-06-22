package com.saunafs.bm.html.presenter;

import java.time.Duration;

import com.saunafs.common.quant.Rate;
import com.saunafs.common.quant.Size;

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

  public static Formatter<Rate> mebibytesPerSecond() {
    return new Formatter<Rate>() {
      public String format(Rate transfer) {
        return "%.3f".formatted(transfer.inMebibytesPerSecond());
      }

      public String unit() {
        return "MiB/s";
      }
    };
  }
}
