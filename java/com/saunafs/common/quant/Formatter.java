package com.saunafs.common.quant;

public interface Formatter<T> {
  public String format(T value);

  public String unit();
}
