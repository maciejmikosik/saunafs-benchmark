package com.saunafs.common.html;

import java.text.DecimalFormat;

public class Em {
  private final double value;

  private Em(double value) {
    this.value = value;
  }

  public static Em em(double value) {
    return new Em(value);
  }

  public Em multiply(double factor) {
    return em(value * factor);
  }

  public Em minus(Em that) {
    return em(this.value - that.value);
  }

  public String toString() {
    return formatter.format(value);
  }

  private static final DecimalFormat formatter = new DecimalFormat("#.##em");
}
