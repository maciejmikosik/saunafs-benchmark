package com.saunafs.proto;

import java.io.DataOutputStream;

public interface Request extends Message {
  public void writeTo(DataOutputStream output);
}
