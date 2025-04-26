package com.saunafs.common;

public class Size {
  private final int bytes;

  private Size(int bytes) {
    this.bytes = bytes;
  }

  public static Size bytes(int bytes) {
    return new Size(bytes);
  }

  public static Size kibibytes(int kibibytes) {
    return bytes(kibibytes << 10);
  }

  public static Size mebibytes(int mebibytes) {
    return bytes(mebibytes << 20);
  }

  public int inBytes() {
    return bytes;
  }
}
