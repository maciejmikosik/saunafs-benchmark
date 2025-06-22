package com.saunafs.common.html;

import static java.util.Objects.requireNonNull;

public class Property {
  public final String name;
  public final String value;

  protected Property(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public static Property property(String name, String value) {
    return new Property(
        requireNonNull(name),
        requireNonNull(value));
  }

  public static Property property(String name, Object value) {
    return property(name, value.toString());
  }
}
