package com.saunafs.common;

public class ProgressBar {
  private final int width;

  private ProgressBar(int width) {
    this.width = width;
  }

  public static ProgressBar progressBar() {
    return progressBar(50);
  }

  public static ProgressBar progressBar(int width) {
    if (width < 1) {
      throw new IllegalArgumentException("width: " + width);
    }
    return new ProgressBar(width);
  }

  public void update(float progress) {
    if (progress < 0f || 1f < progress) {
      throw new IllegalArgumentException("progress: " + progress);
    }
    int bars = (int) (progress * width);
    System.err.print(new StringBuilder()
        .append("\r\033[K")
        .append("[")
        .append("#".repeat(bars))
        .append(" ".repeat(width - bars))
        .append("] ")
        .append((int) (progress * 100))
        .append("%")
        .toString());
  }
}
