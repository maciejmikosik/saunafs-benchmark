package com.saunafs.bm.present;

public class FractionWithThreshold {
  public final double fraction;
  public final double threshold;

  private FractionWithThreshold(double fraction, double threshold) {
    this.fraction = fraction;
    this.threshold = threshold;
  }

  public static FractionWithThreshold fractionWithThreshold(
      double fraction,
      double threshold) {
    return new FractionWithThreshold(fraction, threshold);
  }
}
