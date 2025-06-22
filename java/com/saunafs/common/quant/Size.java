package com.saunafs.common.quant;

public class Size {
  private final long bytes;

  private Size(long bytes) {
    this.bytes = bytes;
  }

  public static Size bytes(long bytes) {
    return new Size(bytes);
  }

  public static Size kibibytes(long kibibytes) {
    return bytes(kibibytes << 10);
  }

  public static Size mebibytes(long mebibytes) {
    return bytes(mebibytes << 20);
  }

  public long inBytes() {
    return bytes;
  }

  public Size plus(Size size) {
    return bytes(bytes + size.bytes);
  }
}
