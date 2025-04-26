package com.saunafs.proto;

import java.io.DataOutputStream;

public interface Message {
  public Description description();

  default void writeTo(DataOutputStream output) {
    throw new UnsupportedOperationException();
  }
}
