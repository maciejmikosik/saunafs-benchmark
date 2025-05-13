package com.saunafs.proto.msn.sniff;

import java.time.Instant;

import com.saunafs.proto.Message;

public class Received extends Event {
  private Received(Message message, Instant time) {
    super(message, time);
  }

  public static Received received(Message message, Instant time) {
    return new Received(message, time);
  }
}
