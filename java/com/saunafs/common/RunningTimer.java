package com.saunafs.common;

import static java.time.Duration.between;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;

public class RunningTimer {
  private final InstantSource clock;
  private final Instant started;

  private RunningTimer(InstantSource clock, Instant started) {
    this.clock = clock;
    this.started = started;
  }

  public static RunningTimer runningTimer(InstantSource clock) {
    return new RunningTimer(clock, clock.instant());
  }

  public Duration stop() {
    return between(started, clock.instant());
  }
}
