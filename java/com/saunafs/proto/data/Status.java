package com.saunafs.proto.data;

public class Status {
  public byte code;

  public static Status status(byte code) {
    var status = new Status();
    status.code = code;
    return status;
  }

  public boolean isOk() {
    return code == 0;
  }
}
