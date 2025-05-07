package com.saunafs.bm;

import java.time.Instant;

import com.saunafs.proto.Message;

public class Sent extends Event {
  private Sent(Message message, Instant time) {
    super(message, time);
  }

  public static Sent sent(Message message, Instant time) {
    return new Sent(message, time);
  }
}
