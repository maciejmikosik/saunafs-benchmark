package com.saunafs.common.html;

import static java.util.Objects.requireNonNull;

public class Attribute {
  public final String name;
  public final String value;

  private Attribute(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public static Attribute attribute(String name, String value) {
    return new Attribute(
        requireNonNull(name),
        requireNonNull(value));
  }
}
