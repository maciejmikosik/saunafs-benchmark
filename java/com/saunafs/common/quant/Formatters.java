package com.saunafs.common.quant;

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
}
