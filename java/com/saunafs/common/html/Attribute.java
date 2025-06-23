package com.saunafs.common.html;

public class Attribute {
  public final String name;
  public final String value;

  protected Attribute(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public static Attribute attribute(String name, String value) {
    return new Attribute(name, value);
  }

  public static Attribute attribute(String name, Object value) {
    return new Attribute(name, value.toString());
  }
}
