package com.saunafs.common;

public class ProgressBar {
  private int width = 50;
  private long max = 1;
  private long count = 0;

  private ProgressBar() {}

  public static ProgressBar progressBar() {
    return new ProgressBar();
  }

  public ProgressBar width(int width) {
    this.width = width;
    return this;
  }

  public ProgressBar max(long max) {
    this.max = max;
    return this;
  }

  public void increment() {
    count++;
    draw();
  }

  public void draw() {
    if (count < 0 || max < count) {
      throw new IllegalArgumentException("progress: " + count);
    }
    var progress = 1.0 * count / max;
    int bars = (int) (progress * width);
    int percentage = (int) (progress * 100);
    System.err.print(new StringBuilder()
        .append("\r\033[K")
        .append("[")
        .append("#".repeat(bars))
        .append(" ".repeat(width - bars))
        .append("] ")
        .append(percentage)
        .append("%")
        .toString());
  }
}
