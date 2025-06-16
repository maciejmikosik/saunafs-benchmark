package com.saunafs.bm.html.presenter;

public interface Formatter<T> {
  public String format(T value);

  public String unit();
}
