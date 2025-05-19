package com.saunafs.common;

import static com.saunafs.common.RunningTimer.runningTimer;

import java.time.InstantSource;

public class Timer {
  private final InstantSource clock;

  private Timer(InstantSource clock) {
    this.clock = clock;
  }

  public static Timer timer(InstantSource clock) {
    return new Timer(clock);
  }

  public RunningTimer start() {
    return runningTimer(clock);
  }
}
