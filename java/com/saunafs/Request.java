package com.saunafs;

import java.io.DataOutputStream;

public interface Request {
  public void writeTo(DataOutputStream output);
}
