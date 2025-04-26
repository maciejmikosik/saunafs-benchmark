package com.saunafs.proto;

public class Description {
  public String identifier;
  public int code;
  public int version;

  private Description() {}

  public static Description description() {
    return new Description();
  }

  public Description identifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  public Description code(int code) {
    this.code = code;
    return this;
  }

  public Description version(int version) {
    this.version = version;
    return this;
  }
}
