package com.saunafs.bm;

import java.time.Instant;

import com.saunafs.proto.Message;

public abstract class Event {
  public final Message message;
  public final Instant time;

  protected Event(Message message, Instant time) {
    this.message = message;
    this.time = time;
  }
}
