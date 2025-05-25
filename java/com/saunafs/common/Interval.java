package com.saunafs.common;

import static java.time.Duration.between;

import java.time.Duration;
import java.time.Instant;

public class Interval {
  public final Instant begin;
  public final Instant end;

  private Interval(Instant begin, Instant end) {
    this.begin = begin;
    this.end = end;
  }

  public static Interval interval(Instant begin, Instant end) {
    return new Interval(begin, end);
  }

  public Duration duration() {
    return between(begin, end);
  }
}
