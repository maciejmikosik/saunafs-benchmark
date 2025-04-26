package com.saunafs.proto;

import java.io.DataInputStream;
import java.util.function.Function;

public class Description {
  public String identifier;
  public int code;
  public int version;
  public Function<DataInputStream, Response> decoder = input -> {
    throw new UnsupportedOperationException();
  };

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

  public Description decoder(Function<DataInputStream, Response> decoder) {
    this.decoder = decoder;
    return this;
  }
}
