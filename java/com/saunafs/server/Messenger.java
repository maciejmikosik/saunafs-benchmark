package com.saunafs.server;

import com.saunafs.proto.Message;

public interface Messenger {
  void send(Message message);

  Message receive();
}
