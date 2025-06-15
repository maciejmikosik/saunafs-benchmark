package com.saunafs.bm.present;

import java.time.Duration;

import com.saunafs.common.quant.Formatter;
import com.saunafs.common.quant.Size;
import com.saunafs.common.quant.Transfer;

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

  public static Formatter<Transfer> mebibytesPerSecond() {
    return new Formatter<Transfer>() {
      public String format(Transfer transfer) {
        return "%.3f".formatted(transfer.inMebibytesPerSecond());
      }

      public String unit() {
        return "MiB/s";
      }
    };
  }
}
