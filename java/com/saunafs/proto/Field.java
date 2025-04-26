package com.saunafs.proto;

public class Field {
  public final Class<?> type;
  public final String name;

  private Field(Class<?> type, String name) {
    this.type = type;
    this.name = name;
  }

  public static Field field(Class<?> type, String name) {
    return new Field(type, name);
  }
}
