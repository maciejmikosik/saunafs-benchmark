package com.saunafs.proto;

public interface Messenger {
  void send(Message message);

  Message receive();
}
