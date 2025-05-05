package com.saunafs.proto;

import java.io.DataOutputStream;

public interface Message {
  default void writeTo(DataOutputStream output) {
    throw new UnsupportedOperationException();
  }
}
