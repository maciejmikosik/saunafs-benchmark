package com.saunafs.proto;

import java.io.DataOutputStream;

public interface Request {
  public void writeTo(DataOutputStream output);
}
