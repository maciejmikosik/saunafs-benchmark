package com.saunafs.proto;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Description {
  public String identifier;
  public int code;
  public int version;
  public List<Field> fields = new ArrayList<>();

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

  public Description field(Field field) {
    fields.add(field);
    return this;
  }

  public Description field(Class<?> type, String name) {
    return field(Field.field(type, name));
  }

  public Description decoder(Function<DataInputStream, Response> decoder) {
    this.decoder = decoder;
    return this;
  }
}
